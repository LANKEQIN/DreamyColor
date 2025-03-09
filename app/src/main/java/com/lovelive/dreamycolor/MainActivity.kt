@file:OptIn(ExperimentalFoundationApi::class)

package com.lovelive.dreamycolor

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

import androidx.compose.foundation.lazy.grid.GridItemSpan
import com.lovelive.dreamycolor.utils.PinyinUtils
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.grid.rememberLazyGridState

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp

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
            Screen.Profile // 添加 Profile 屏幕
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
                        currentScreen = null
                    }
                )
                "voice_actor_detail" -> VoiceActorDetailScreen(
                    voiceActorName = voiceActorName,
                    onBackPressed = {
                        currentScreen = null
                    }
                )
                null -> {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                                3 -> ProfileScreen(settingsManager = settingsManager) // 添加 ProfileScreen
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


@SuppressLint("MutableCollectionMutableState")
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
    var selectedCharacter by remember { mutableStateOf<CharacterCard?>(null) }
    var selectedVoiceActor by remember { mutableStateOf<VoiceActorCard?>(null) }
    val showCoefficient by settingsManager.showCoefficientFlow.collectAsState(initial = false)
    val showPinyin by settingsManager.showPinyinFlow.collectAsState(initial = false)
    var currentDimension by rememberSaveable { mutableStateOf(initialDimension) }
    var previousDimension by rememberSaveable { mutableStateOf(initialDimension) }
    var expandedGroups by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    LaunchedEffect(currentDimension) {
        onDimensionChange(currentDimension)
    }

    val currentScrollPosition = remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    LaunchedEffect(currentScrollPosition.value) {
        if (currentScrollPosition.value != initialScrollPosition) {
            onScrollPositionChange(currentScrollPosition.value)
        }
    }

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

    val groupedCharacters by viewModel.getCharactersByGroup().collectAsState(initial = emptyMap())
    val groupedVoiceActors by viewModel.getVoiceActorsByGroup().collectAsState(initial = emptyMap())

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

    // 提取滚动监听逻辑，减少不必要的重组
    var isFabVisible by remember { mutableStateOf(true) }
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isFabVisible = false
        } else {
            delay(200) // 减少延迟时间以提升响应速度
            isFabVisible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), // 控制卡片之间的垂直间距
                horizontalArrangement = Arrangement.spacedBy(16.dp), // 控制卡片之间的水平间距
                state = listState
            ) {
                items(
                    items = if (currentDimension == "角色") characterItems else voiceActorItems,
                    span = { item ->
                        when (item) {
                            is GroupItem.Header -> GridItemSpan(2)
                            else -> GridItemSpan(1)
                        }
                    },
                    key = { item -> 
                        when (item) {
                            is GroupItem.Header -> item.title
                            is GroupItem.Character -> item.data.name
                            is GroupItem.VoiceActor -> item.data.name
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
                            }
                        )
                        is GroupItem.Character -> CharacterItem(
                            character = item.data,
                            showPinyin = showPinyin,
                            expandedGroups = expandedGroups,
                            groupedCharacters = groupedCharacters,
                            onClick = { character ->
                                previousDimension = currentDimension
                                selectedCharacter = character
                            }
                        )
                        is GroupItem.VoiceActor -> VoiceActorItem(
                            voiceActor = item.data,
                            showCoefficient = showCoefficient,
                            showPinyin = showPinyin,
                            expandedGroups = expandedGroups,
                            groupedVoiceActors = groupedVoiceActors,
                            onClick = { voiceActor ->
                                previousDimension = currentDimension
                                selectedVoiceActor = voiceActor
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isFabVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200)),
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

    selectedCharacter?.let { character ->
        CharacterOptionsDialog(
            character = character,
            onDismiss = {
                selectedCharacter = null
                currentDimension = previousDimension
            },
            onLocalPageClick = {
                selectedCharacter = null
                currentDimension = previousDimension
                onCharacterClick(character.name)
            },
            onExternalWikiClick = {
                selectedCharacter = null
                currentDimension = previousDimension
                val url = "https://mzh.moegirl.org.cn/${character.name}"
                context.openInBrowser(url)
            }
        )
    }

    selectedVoiceActor?.let { voiceActor ->
        VoiceActorOptionsDialog(
            voiceActor = voiceActor,
            onDismiss = {
                selectedVoiceActor = null
                currentDimension = previousDimension
            },
            onLocalPageClick = {
                selectedVoiceActor = null
                currentDimension = previousDimension
                onVoiceActorClick(voiceActor.name)
            },
            onExternalWikiClick = {
                selectedVoiceActor = null
                currentDimension = previousDimension
                val url = "https://mzh.moegirl.org.cn/${voiceActor.name}"
                context.openInBrowser(url)
            }
        )
    }
}


// 提取角色项逻辑以减少滚动时的计算开销
@Composable
private fun CharacterItem(
    character: CharacterCard,
    showPinyin: Boolean,
    expandedGroups: Map<String, Boolean>,
    groupedCharacters: Map<String, List<CharacterCard>>,
    onClick: (CharacterCard) -> Unit
) {
    AnimatedVisibility(
        visible = expandedGroups[groupedCharacters.entries.find { it.value.contains(character) }?.key] ?: true,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        CharacterCardUI(
            character = character,
            showPinyin = showPinyin,
            onClick = onClick
        )
    }
}

// 提取声优项逻辑以减少滚动时的计算开销
@Composable
private fun VoiceActorItem(
    voiceActor: VoiceActorCard,
    showCoefficient: Boolean,
    showPinyin: Boolean,
    expandedGroups: Map<String, Boolean>,
    groupedVoiceActors: Map<String, List<VoiceActorCard>>,
    onClick: (VoiceActorCard) -> Unit
) {
    AnimatedVisibility(
        visible = expandedGroups[groupedVoiceActors.entries.find { it.value.contains(voiceActor) }?.key] ?: true,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        VoiceActorCardUI(
            voiceActor = voiceActor,
            showCoefficient = showCoefficient,
            showPinyin = showPinyin,
            onClick = onClick
        )
    }
}

// 新增：分组标题组件
@Composable
fun GroupHeader(
    title: String,
    expanded: Boolean = true,
    onExpandedChange: () -> Unit = {},
    verticalPadding: Dp = 12.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.medium
            )
            .padding(horizontal = 12.dp, vertical = verticalPadding)
            .clickable { onExpandedChange() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 200))
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
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
                    showPinyin -> 300.dp
                    else -> 240.dp
                }
            )
            // 移除水波纹效果，保留点击事件
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(voiceActor) },
                    onLongPress = {
                        context.copyToClipboard("${voiceActor.name}\n${voiceActor.japaneseName}")
                    }
                )
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
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
                    showPinyin -> 300.dp
                    else -> 240.dp
                }
            )
            // 移除水波纹效果，保留点击事件
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick(character) },
                    onLongPress = {
                        context.copyToClipboard("${character.name}\n${character.japaneseName}")
                    }
                )
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = MaterialTheme.shapes.large,
        // 使用与VoiceActorCardUI相同的默认颜色
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
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
    Column(modifier = Modifier.height(if (showPinyin) 115.dp else 60.dp)) {
        // 中文名称
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 19.sp,
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
                        fontSize = 13.sp,  // 拼音字体
                        fontWeight = FontWeight.Normal
                    ),
                    lineHeight = 19.sp
                )
            }
        }

        // 日文名显示（保持现有的罗马音转换）
        Text(
            text = if (showPinyin) PinyinUtils.convertJapaneseToRomaji(japaneseName) else japaneseName,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 13.sp
            ),
            lineHeight = if (showPinyin) 18.sp else 18.sp
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

