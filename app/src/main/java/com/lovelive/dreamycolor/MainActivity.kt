@file:OptIn(ExperimentalFoundationApi::class)

package com.lovelive.dreamycolor

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovelive.dreamycolor.data.repository.EncyclopediaRepository
import com.lovelive.dreamycolor.database.EncyclopediaDatabase
import com.lovelive.dreamycolor.model.CharacterCard
import com.lovelive.dreamycolor.model.VoiceActorCard
import com.lovelive.dreamycolor.ui.theme.DreamyColorTheme
import com.lovelive.dreamycolor.utils.copyToClipboard
import com.lovelive.dreamycolor.viewmodel.EncyclopediaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.lovelive.dreamycolor.utils.PinyinUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState

// 用于对话框配置的数据类
data class DialogConfig(
    val title: String,
    val message: String,
    val confirmText: String,
    val confirmAction: () -> Unit,
    val dismissText: String,
    val dismissAction: () -> Unit
)

class MainActivity : ComponentActivity() {
    private val settingsManager by lazy { SettingsManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsManager.themeModeFlow.collectAsState(
                initial = SettingsManager.ThemeMode.FOLLOW_SYSTEM
            )
            val textSize by settingsManager.textSizeFlow.collectAsState(
                initial = SettingsManager.TextSize.FOLLOW_SYSTEM
            )
            val isDarkTheme = when (themeMode) {
                SettingsManager.ThemeMode.LIGHT -> false
                SettingsManager.ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }
            
            // 收集主题颜色设置
            val colorTheme by settingsManager.colorThemeFlow.collectAsState(initial = SettingsManager.ColorTheme.MATERIAL_YOU)

            // 动态设置状态栏文字颜色
            LaunchedEffect(isDarkTheme) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                }
            }

            DreamyColorTheme(
                themeMode = themeMode,
                textSize = textSize,
                colorTheme = colorTheme
            ) {
                // 使用rememberSaveable保持屏幕旋转后的状态
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    // 主内容始终在底部
                    MainContent(settingsManager = settingsManager)

                    // 开屏动画覆盖在上面，逐渐消失
                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(800))
                    ) {
                        SplashScreen(
                            onTimeout = { showSplash = false },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stringResource(R.string.splash_text).forEach { char ->
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        LaunchedEffect(Unit) {
            delay(1500L)
            onTimeout()
        }
    }
}


// 主界面内容
@Composable
fun MainContent(settingsManager: SettingsManager) {
    val items = remember {
        listOf(
            Screen.Exclusive,
            Screen.Inspiration,
            Screen.Encyclopedia,
            Screen.Profile
        )
    }
    // 添加导航状态
    var currentScreen by rememberSaveable { mutableStateOf<String?>(null) }
    var characterName by rememberSaveable { mutableStateOf("") }
    var voiceActorName by rememberSaveable { mutableStateOf("") }
    // 添加这个变量来记录百科卡片列表的滚动位置
    var encyclopediaScrollPosition by rememberSaveable { mutableIntStateOf(0) }
    var encyclopediaDimension by rememberSaveable { mutableStateOf("角色") }
    // 添加Tab选择状态作为主导航机制
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    // 使用rememberSaveable保持页面状态在配置更改时不丢失
    val pagerState = rememberPagerState(pageCount = { items.size }, initialPage = selectedTabIndex)

    // 确保pagerState和selectedTabIndex保持同步
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTabIndex) {
            selectedTabIndex = pagerState.currentPage
        }
    }
    Scaffold(
        bottomBar = {
            // 只在主界面显示底部导航栏
            if (currentScreen == null) {
                NavigationBar {
                    items.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = {},
                            label = {
                                val textStyle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    MaterialTheme.typography.labelMedium
                                } else {
                                    LocalTextStyle.current
                                }
                                Text(
                                    text = stringResource(id = screen.titleRes),
                                    style = textStyle.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 页面切换动画
        AnimatedContent(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding),
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 300))
            }
        ) { screen ->
            when (screen) {
                "character_detail" -> CharacterDetailScreen(
                    characterName = characterName,
                    onBackPressed = {
                        // 返回到列表而不是直接退出
                        currentScreen = null
                    }
                )
                "voice_actor_detail" -> VoiceActorDetailScreen(
                    voiceActorName = voiceActorName,
                    onBackPressed = {
                        // 返回到列表而不是直接退出
                        currentScreen = null
                    }
                )
                null -> {
                    // 显示主界面
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 使用AnimatedContent切换Tab页面
                        AnimatedContent(
                            targetState = selectedTabIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                                        fadeOut(animationSpec = tween(durationMillis = 300))
                            }
                        ) { page ->
                            when (page) {
                                0 -> ExclusiveScreen()
                                1 -> InspirationScreen()
                                2 -> EncyclopediaScreen(
                                    onCharacterClick = { name ->
                                        characterName = name
                                        currentScreen = "character_detail"
                                    },
                                    onVoiceActorClick = { name ->
                                        voiceActorName = name
                                        currentScreen = "voice_actor_detail"
                                    },
                                    initialScrollPosition = encyclopediaScrollPosition,
                                    onScrollPositionChange = { position ->
                                        encyclopediaScrollPosition = position
                                    },
                                    initialDimension = encyclopediaDimension,
                                    onDimensionChange = { dimension ->
                                        encyclopediaDimension = dimension
                                    }
                                )
                                3 -> ProfileScreen(settingsManager)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Website数据类和相关函数已迁移到InspirationScreen.kt



// InspirationScreen已迁移到InspirationScreen.kt文件中


// 相关子屏幕已迁移到InspirationScreen.kt文件中

sealed class GroupItem {
    data class Header(val title: String) : GroupItem()
    data class Character(val data: CharacterCard) : GroupItem()
    data class VoiceActor(val data: VoiceActorCard) : GroupItem()
}


@Composable
fun EncyclopediaScreen(
    onCharacterClick: (String) -> Unit = {},
    onVoiceActorClick: (String) -> Unit = {},
    initialScrollPosition: Int = 0,
    onScrollPositionChange: (Int) -> Unit = {},
    initialDimension: String = "角色",
    onDimensionChange: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { EncyclopediaDatabase.getDatabase(context) }
    val repository = remember { EncyclopediaRepository(database.encyclopediaDao()) }
    val settingsManager = remember { SettingsManager(context) }
    val listState = rememberLazyGridState(initialFirstVisibleItemIndex = initialScrollPosition)
    // 添加对话框状态管理
    var selectedCharacter by remember { mutableStateOf<CharacterCard?>(null) }
    var selectedVoiceActor by remember { mutableStateOf<VoiceActorCard?>(null) }
    // 添加新的拼音设置状态
    val showCoefficient by settingsManager.showCoefficientFlow.collectAsState(initial = false)
    val showPinyin by settingsManager.showPinyinFlow.collectAsState(initial = false)
    // 使用传入的初始维度
    var currentDimension by rememberSaveable { mutableStateOf(initialDimension) }
    var previousDimension by rememberSaveable { mutableStateOf(initialDimension) }
    // 添加分组展开状态管理
    var expandedGroups by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    // 添加一个key来强制重组
    var updateKey by remember { mutableStateOf(0) }
    // 当维度变化时通知父组件
    LaunchedEffect(currentDimension) {
        onDimensionChange(currentDimension)
    }
    // 监听列表滚动位置变化并上报
    val currentScrollPosition = remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    LaunchedEffect(currentScrollPosition.value) {
        // 只有当滚动位置变化且不是初始化时才上报
        if (currentScrollPosition.value != initialScrollPosition) {
            onScrollPositionChange(currentScrollPosition.value)
        }
    }
    // 使用工厂方法创建ViewModel
    val viewModel: EncyclopediaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EncyclopediaViewModel(
                    context.applicationContext as Application,
                    repository
                ) as T
            }
        }
    )
    // 收集状态流
    val groupedCharacters by viewModel.getCharactersByGroup().collectAsState(initial = emptyMap())
    val groupedVoiceActors by viewModel.getVoiceActorsByGroup().collectAsState(initial = emptyMap())
    val scrollState = rememberScrollState()
    var isFabVisible by remember { mutableStateOf(true) }
    // 新增：构建带分组的列表数据
    val characterItems = remember(groupedCharacters) {
        groupedCharacters.flatMap { (group, list) ->
            listOf(GroupItem.Header(group)) + list.map { GroupItem.Character(it) }
        }
    }
    val voiceActorItems = remember(groupedVoiceActors) {
        groupedVoiceActors.flatMap { (group, list) ->
            listOf(GroupItem.Header(group)) + list.map { GroupItem.VoiceActor(it) }
        }
    }
    // 监听滚动状态控制FAB可见性
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isFabVisible = false
        } else {
            delay(500) // 停止滚动后延迟显示
            isFabVisible = true
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 切换按钮区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.medium
                        )
                        .padding(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DimensionButton(
                            text = "角色",
                            selected = currentDimension == "角色",
                            onClick = { currentDimension = "角色" }
                        )
                        DimensionButton(
                            text = "声优",
                            selected = currentDimension == "声优",
                            onClick = { currentDimension = "声优" }
                        )
                    }
                }
            }
            // 主内容区改用单 LazyGrid，使用state参数保持滚动位置
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                state = listState
            ) {
                items(
                    items = if (currentDimension == "角色") characterItems else voiceActorItems,
                    span = { item ->
                        when (item) {
                            is GroupItem.Header -> GridItemSpan(2) // 标题跨2列
                            else -> GridItemSpan(1) // 内容项占1列
                        }
                    }
                ) { item ->
                    when (item) {
                        is GroupItem.Header -> GroupHeader(
                            title = item.title,
                            expanded = expandedGroups[item.title] ?: true,
                            onExpandedChange = {
                                expandedGroups = expandedGroups.toMutableMap().apply {
                                    this[item.title] = !(this[item.title] ?: true)
                                }
                                updateKey++ // 增加key触发重组
                            }
                        )
                        is GroupItem.Character -> AnimatedVisibility(
                            visible = expandedGroups[groupedCharacters.entries.find { it.value.contains(item.data) }?.key] ?: true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            CharacterCardUI(
                                character = item.data,
                                showPinyin = showPinyin,
                                onClick = { character ->
                                    previousDimension = currentDimension
                                    selectedCharacter = character
                                }
                            )
                        }
                        is GroupItem.VoiceActor -> AnimatedVisibility(
                            visible = expandedGroups[groupedVoiceActors.entries.find { it.value.contains(item.data) }?.key] ?: true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            VoiceActorCardUI(
                                voiceActor = item.data,
                                showCoefficient = showCoefficient,
                                showPinyin = showPinyin,
                                onClick = { voiceActor ->
                                    previousDimension = currentDimension
                                    selectedVoiceActor = voiceActor
                                }
                            )
                        }
                    }
                }
            }
        }
        // 悬浮刷新按钮
        AnimatedVisibility(
            visible = isFabVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { viewModel.refreshData(context) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "刷新数据",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    // 角色详情对话框
    selectedCharacter?.let { character ->
        CharacterOptionsDialog(
            character = character,
            onDismiss = {
                selectedCharacter = null
                currentDimension = previousDimension // 恢复之前的维度
            },
            onLocalPageClick = {
                selectedCharacter = null
                currentDimension = previousDimension // 恢复之前的维度
                onCharacterClick(character.name)
            },
            onExternalWikiClick = {
                selectedCharacter = null
                currentDimension = previousDimension // 恢复之前的维度
                val url = "https://mzh.moegirl.org.cn/${character.name}"
                context.openInBrowser(url)
            }
        )
    }
    // 声优详情对话框
    selectedVoiceActor?.let { voiceActor ->
        VoiceActorOptionsDialog(
            voiceActor = voiceActor,
            onDismiss = {
                selectedVoiceActor = null
                currentDimension = previousDimension // 恢复之前的维度
            },
            onLocalPageClick = {
                selectedVoiceActor = null
                currentDimension = previousDimension // 恢复之前的维度
                onVoiceActorClick(voiceActor.name)
            },
            onExternalWikiClick = {
                selectedVoiceActor = null
                currentDimension = previousDimension // 恢复之前的维度
                val url = "https://mzh.moegirl.org.cn/${voiceActor.name}"
                context.openInBrowser(url)
            }
        )
    }
}

// 新增：分组标题组件
@Composable
fun GroupHeader(title: String, expanded: Boolean = true, onExpandedChange: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable { onExpandedChange() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "折叠" else "展开",
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun DimensionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CharacterOptionsDialog(
    character: CharacterCard,
    onDismiss: () -> Unit,
    onLocalPageClick: () -> Unit,
    onExternalWikiClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("查看${character.name}的详细信息") },
        text = { Text("请选择查看方式：") },
        confirmButton = {
            Button(onClick = onLocalPageClick) {
                Text("本地页面")
            }
        },
        dismissButton = {
            Button(onClick = onExternalWikiClick) {
                Text("萌娘百科")
            }
        }
    )
}

@Composable
fun VoiceActorOptionsDialog(
    voiceActor: VoiceActorCard,
    onDismiss: () -> Unit,
    onLocalPageClick: () -> Unit,
    onExternalWikiClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("查看${voiceActor.name}的详细信息") },
        text = { Text("请选择查看方式：") },
        confirmButton = {
            Button(onClick = onLocalPageClick) {
                Text("本地页面")
            }
        },
        dismissButton = {
            Button(onClick = onExternalWikiClick) {
                Text("萌娘百科")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoiceActorCardUI(
    voiceActor: VoiceActorCard,
    showCoefficient: Boolean,
    showPinyin: Boolean = false,
    onClick: (VoiceActorCard) -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    showCoefficient && showPinyin -> 320.dp // 同时显示系数和拼音时的高度
                    showCoefficient -> 280.dp
                    showPinyin -> 270.dp
                    else -> 230.dp
                }
            )
            .combinedClickable(
                onClick = {
                    onClick(voiceActor)
                },
                onLongClick = {
                    context.copyToClipboard("${voiceActor.name}\n${voiceActor.japaneseName}")
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题区域
            NameSection(
                name = voiceActor.name,
                japaneseName = voiceActor.japaneseName,
                showPinyin = showPinyin
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            // 信息区域
            GridLayout(
                listOfNotNull(
                    "生日" to voiceActor.birthday,
                    "事务所" to voiceActor.agency,
                    if (showCoefficient) "系数" to voiceActor.coefficient else null
                )
            )
            // 删除描述区域，不再显示简介文字
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterCardUI(
    character: CharacterCard,
    onClick: (CharacterCard) -> Unit = {},
    showPinyin: Boolean = false
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    showPinyin -> 270.dp
                    else -> 240.dp
                }
            )
            .combinedClickable(
                onClick = {
                    onClick(character)
                },
                onLongClick = {
                    context.copyToClipboard("${character.name}\n${character.japaneseName}")
                }
            ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large,
        // 使用与VoiceActorCardUI相同的默认颜色
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 名称区域
            NameSection(
                name = character.name,
                japaneseName = character.japaneseName,
                showPinyin = showPinyin
            )
            // 分割线
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            // 信息网格
            GridLayout(
                listOfNotNull(
                    "生日" to character.birthday,
                    "年级" to character.schoolYear,
                )
            )
            // 删除描述区域，不再显示简介文字
        }
    }
}


@Composable
private fun NameSection(name: String, japaneseName: String, showPinyin: Boolean = false) {
    Column(modifier = Modifier.height(if (showPinyin) 90.dp else 60.dp)) {
        // 中文名称
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 如果开启拼音显示，且能从映射表找到对应拼音，则显示拼音
        if (showPinyin) {
            PinyinUtils.chinesePinyinMap[name]?.let { pinyin ->
                Text(
                    text = pinyin,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontSize = 12.sp,  // 拼音字体
                        fontWeight = FontWeight.Normal
                    ),
                    lineHeight = 16.sp
                )
            }
        }

        // 日文名显示（保持现有的罗马音转换）
        Text(
            text = if (showPinyin) PinyinUtils.convertJapaneseToRomaji(japaneseName) else japaneseName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            ),
            lineHeight = if (showPinyin) 24.sp else 18.sp
        )
    }
}




@Composable
private fun GridLayout(items: List<Pair<String, String>>) {
    // 改为单列布局
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 直接遍历所有项目，不需要行列计算
        items.forEach { item ->
            InfoItem(
                label = item.first,
                value = item.second,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp)
            .combinedClickable(
                onClick = { /* 普通点击不做处理 */ },
                onLongClick = {
                    context.copyToClipboard(value)
                }
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}



@Composable
fun ProfileScreen(settingsManager: SettingsManager) {
    // 状态管理 - 使用rememberSaveable保持配置变更
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showTextSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showDisclaimer by rememberSaveable { mutableStateOf(false) }
    var remainingTime by remember { mutableIntStateOf(7) }
    var showDarkRealmSnackbar by remember { mutableStateOf(false) }

    // 状态收集
    val themeMode by settingsManager.themeModeFlow.collectAsState(
        initial = SettingsManager.ThemeMode.FOLLOW_SYSTEM
    )
    val textSize by settingsManager.textSizeFlow.collectAsState(
        initial = SettingsManager.TextSize.FOLLOW_SYSTEM
    )
    val showPinyin by settingsManager.showPinyinFlow.collectAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 主布局：使用垂直滚动以适应小屏幕
    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容区：使用垂直滚动
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 版本号条目（点击后满足条件触发免责声明）
            VersionEntry(
                versionName = getVersionName(context),
                onSecretActivated = { showDisclaimer = true }
            )

            // 文字大小设置
            TextSizeSettingCard(
                currentSize = textSize,
                onClick = { showTextSizeDialog = true }
            )

            // 主题设置长条
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "主题模式",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (themeMode) {
                                SettingsManager.ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                                SettingsManager.ThemeMode.LIGHT -> "浅色"
                                SettingsManager.ThemeMode.DARK -> "深色"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "箭头",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 拼音设置卡片 - 独立卡片，不再嵌套在主题卡片内
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            settingsManager.setShowPinyin(!showPinyin)
                        }
                    },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "百科卡片显示拼音及罗马音",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Switch(
                        checked = showPinyin,
                        onCheckedChange = {
                            coroutineScope.launch {
                                settingsManager.setShowPinyin(it)
                            }
                        }
                    )
                }
            }
            
            // 主题颜色设置卡片
            val colorTheme by settingsManager.colorThemeFlow.collectAsState(initial = SettingsManager.ColorTheme.MATERIAL_YOU)
            var showColorThemeDialog by rememberSaveable { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showColorThemeDialog = true },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "主题颜色",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (colorTheme) {
                                SettingsManager.ColorTheme.MATERIAL_YOU -> "Material You"
                                SettingsManager.ColorTheme.PURPLE -> "奇迹紫色"
                                SettingsManager.ColorTheme.ROSE -> "丹红色"
                                SettingsManager.ColorTheme.LIGHT_BLUE -> "浅蓝色"
                                SettingsManager.ColorTheme.ORANGE -> "橙色"
                                SettingsManager.ColorTheme.DEEP_BLUE -> "深蓝色"
                                SettingsManager.ColorTheme.YELLOW -> "黄色"
                                SettingsManager.ColorTheme.PINK -> "粉色"
                                SettingsManager.ColorTheme.GREEN -> "绿色"
                                SettingsManager.ColorTheme.WHITE -> "白色"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "箭头",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (showColorThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showColorThemeDialog = false },
                    title = {
                        Text(
                            text = "选择主题颜色",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SettingsManager.ColorTheme.entries.forEach { theme ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = colorTheme == theme,
                                            onClick = {
                                                coroutineScope.launch {
                                                    settingsManager.setColorTheme(theme)
                                                    showColorThemeDialog = false
                                                }
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = colorTheme == theme,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (theme) {
                                            SettingsManager.ColorTheme.MATERIAL_YOU -> "Material You"
                                            SettingsManager.ColorTheme.PURPLE -> "奇迹紫色"
                                            SettingsManager.ColorTheme.ROSE -> "丹红色"
                                            SettingsManager.ColorTheme.LIGHT_BLUE -> "浅蓝色"
                                            SettingsManager.ColorTheme.ORANGE -> "橙色"
                                            SettingsManager.ColorTheme.DEEP_BLUE -> "深蓝色"
                                            SettingsManager.ColorTheme.YELLOW -> "黄色"
                                            SettingsManager.ColorTheme.PINK -> "粉色"
                                            SettingsManager.ColorTheme.GREEN -> "绿色"
                                            SettingsManager.ColorTheme.WHITE -> "白色"
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showColorThemeDialog = false }) {
                            Text("关闭")
                        }
                    }
                )
            }
        }

        // Snackbar主机
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // 文字大小对话框
    if (showTextSizeDialog) {
        TextSizeSelectionDialog(
            currentSize = textSize,
            onDismiss = { showTextSizeDialog = false },
            onSizeSelected = { size ->
                coroutineScope.launch {
                    settingsManager.setTextSize(size)
                }
                showTextSizeDialog = false
            }
        )
    }

    // 主题选择对话框
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = themeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { mode ->
                coroutineScope.launch {
                    settingsManager.setThemeMode(mode)
                }
            }
        )
    }

    // 免责声明对话框
    if (showDisclaimer) {
        DisclaimerDialog(
            remainingTime = remainingTime,
            onConfirm = {
                coroutineScope.launch {
                    settingsManager.setShowCoefficient(true)
                }
                showDisclaimer = false
                showDarkRealmSnackbar = true
            },
            onDismiss = {
                showDisclaimer = false
                remainingTime = 7 // 重置倒计时
            }
        ) { newTime ->
            remainingTime = newTime
        }
    }

    // 显示提示"您已进入黑暗领域"
    if (showDarkRealmSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("您已进入黑暗领域")
            showDarkRealmSnackbar = false
        }
    }
}


@Composable
private fun TextSizeSettingCard(
    currentSize: SettingsManager.TextSize,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "文字大小",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (currentSize) {
                        SettingsManager.TextSize.FOLLOW_SYSTEM -> "跟随系统"
                        SettingsManager.TextSize.SMALL -> "小号"
                        SettingsManager.TextSize.MEDIUM -> "中号"
                        SettingsManager.TextSize.LARGE -> "大号"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "箭头",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun TextSizeSelectionDialog(
    currentSize: SettingsManager.TextSize,
    onDismiss: () -> Unit,
    onSizeSelected: (SettingsManager.TextSize) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择文字大小") },
        text = {
            Column {
                SettingsManager.TextSize.entries.forEach { size ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSizeSelected(size) }
                    ) {
                        RadioButton(
                            selected = size == currentSize,
                            onClick = { onSizeSelected(size) }
                        )
                        Text(
                            text = when (size) {
                                SettingsManager.TextSize.FOLLOW_SYSTEM -> "跟随系统 (默认)"
                                SettingsManager.TextSize.SMALL -> "小号 (更紧凑)"
                                SettingsManager.TextSize.MEDIUM -> "中号 (推荐)"
                                SettingsManager.TextSize.LARGE -> "大号 (更易读)"
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}



@Composable
private fun VersionEntry(
    versionName: String,
    onSecretActivated: () -> Unit
) {
    // 使用remember记住点击状态
    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val now = System.currentTimeMillis()
                // 检测连续点击
                if (now - lastClickTime < 1000) {
                    clickCount++
                    if (clickCount >= 7) {
                        onSecretActivated()
                        clickCount = 0
                    }
                } else {
                    clickCount = 1
                }
                lastClickTime = now
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "版本号",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun DisclaimerDialog(
    remainingTime: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    updateTime: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        // 禁止点击对话框外部和返回键自动 dismiss
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        title = {
            Text(
                text = "⚠️ 免责声明",
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text("你想成为Z87吗？")
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "请仔细阅读条款（剩余 ${remainingTime}s）",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = remainingTime <= 0
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 倒计时处理
    LaunchedEffect(Unit) {
        for (i in 7 downTo 1) {
            updateTime(i)
            delay(1000)
        }
        updateTime(0)
    }
}

// 获取版本号的辅助函数
private fun getVersionName(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }
}

@Composable
fun ThemeSelectionDialog(
    currentMode: SettingsManager.ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (SettingsManager.ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题") },
        text = {
            Column {
                // 使用for循环优化性能
                for (mode in SettingsManager.ThemeMode.entries) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(mode) }
                            .padding(16.dp)
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onThemeSelected(mode) }
                        )
                        Text(
                            text = when (mode) {
                                SettingsManager.ThemeMode.FOLLOW_SYSTEM -> "跟随系统"
                                SettingsManager.ThemeMode.LIGHT -> "浅色模式"
                                SettingsManager.ThemeMode.DARK -> "深色模式"
                            },
                            modifier = Modifier.padding(start = 20.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
