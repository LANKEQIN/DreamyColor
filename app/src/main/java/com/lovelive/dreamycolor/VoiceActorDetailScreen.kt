package com.lovelive.dreamycolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovelive.dreamycolor.model.VoiceActorDetail
import com.lovelive.dreamycolor.utils.DetailJsonUtils

import androidx.activity.compose.BackHandler
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceActorDetailScreen(
    voiceActorName: String,
    onBackPressed: () -> Unit
) {
    BackHandler(onBack = onBackPressed)

    val context = LocalContext.current
    var voiceActorDetail by remember { mutableStateOf<VoiceActorDetail?>(null) }
    val themeColor = MaterialTheme.colorScheme.primary

    // 加载声优详情数据
    LaunchedEffect(voiceActorName) {
        // 根据声优名称确定要加载的JSON文件
        val fileName = when (voiceActorName) {
            "新田惠海" -> "1_emi_detail.json"
            "内田彩" -> "1_uchida_detail.json"
            "三森铃子" -> "1_mimori_detail.json"
            "饭田里穗" -> "1_iida_detail.json"
            "久保由利香" -> "1_kubo_detail.json"
            "Pile" -> "1_pile_detail.json"
            "德井青空" -> "1_tokui_detail.json"
            "南条爱乃" -> "1_nanjou_detail.json"
            "楠田亚衣奈" -> "1_kusuda_detail.json"

            "逢田梨香子" -> "2_aida_rikako_detail.json"
            "伊波杏树"  -> "2_inami_anju_detail.json"
            "齐藤朱夏"  -> "2_saito_shuka_detail.json"
            "诹访奈奈香"  -> "2_suwa_nanaka_detail.json"
            "小宫有纱" -> "2_komiya_arisha_detail.json"
            "铃木爱奈" -> "2_suzuki_aina_detail.json"
            "小林爱香" -> "2_kobayashi_aika_detail.json"
            "高槻加奈子" -> "2_takatsuki_kanako_detail.json"
            "降幡爱" -> "2_furihata_ai_detail.json"

            "矢野妃菜喜" -> "3_yano_hinaki_detail.json"
            "大西亚玖璃" -> "3_onishi_aguri_detail.json"
            "相良茉优" -> "3_sagara_mayu_detail.json"
            "前田佳织里" -> "3_maeda_kaori_detail.json"
            "久保田未梦" -> "3_kubota_miyu_detail.json"
            "村上奈津实" -> "3_murakami_natsumi_detail.json"
            "鬼头明里" -> "3_kito_akari_detail.json"
            "指出毬亚" -> "3_sashide_maria_detail.json"
            "田中千惠美" -> "3_tanaka_chiemi_detail.json"
            "小泉萌香" -> "3_koizumi_moeka_detail.json"
            "法元明菜"  -> "3_houmoto_akina_detail.json"
            "内田秀" -> "3_uchida_shu_detail.json"
            "楠木灯" -> "3_kusunoki_tomori_detail.json"
            "林鼓子" -> "3_hayashi_koko_detail.json"

            "伊达小百合" -> "4_data_sayuri_detail.json"
            "Liyuu" -> "4_liyuu_detail.json"
            "Payton尚未" -> "4_payton_naomi_detail.json"
            "岬奈子" -> "4_misaki_nako_detail.json"
            "青山渚" -> "4_aoyama_nagisa_detail.json"
            "铃原希实" -> "4_suzuhara_nozomi_detail.json"
            "薮岛朱音" -> "4_yabushima_akane_detail.json"
            "大熊和奏" -> "4_okuma_wakana_detail.json"
            "绘森彩" -> "4_emori_aya_detail.json"
            "坂仓花" -> "4_sakakura_hana_detail.json"
            "结那" -> "4_yuna_detail.json"

            else -> null
        }

        if (fileName != null) {
            voiceActorDetail = DetailJsonUtils.loadVoiceActorDetail(context, fileName)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(voiceActorName) },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = themeColor.copy(alpha = 0.1f)
            )
        )

        voiceActorDetail?.let { detail ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 头部区域
                VoiceActorHeader(detail, themeColor)

                // 职业信息区域
                SectionTitle(title = "职业信息", color = themeColor)
                InfoItem(label = "事务所", value = detail.career.agency, color = themeColor)
                InfoItem(label = "出道年份", value = detail.career.debutYear.toString(), color = themeColor)

                // 成就区域
                if (detail.career.achievements.isNotEmpty()) {
                    SectionTitle(title = "主要成就", color = themeColor)
                    detail.career.achievements.forEach { achievement ->
                        AchievementItem(text = achievement)
                    }
                }

                // 唱片区域
                SectionTitle(title = "唱片作品", color = themeColor)
                InfoItem(label = "代表专辑", value = detail.discography.bestAlbum, color = themeColor)

                // 角色歌区域
                if (detail.discography.characterSongs.isNotEmpty()) {
                    SectionTitle(title = "角色歌曲", color = themeColor)
                    detail.discography.characterSongs.forEach { song ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColor.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = themeColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "角色: ${song.character}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 底部空间
                Spacer(modifier = Modifier.height(24.dp))
            }
        } ?: run {
            // 加载中或无数据状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun VoiceActorHeader(
    detail: VoiceActorDetail,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(themeColor.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 声优头像（占位）
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(themeColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = detail.voiceActorName.take(1),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = themeColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 声优名称
        DetailHeader(
            title = detail.voiceActorName,
            themeColor = themeColor
        )

        // 声优标签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AttributeChip(
                text = "声优",
                color = themeColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            AttributeChip(
                text = "歌手",
                color = themeColor
            )
        }
    }
}


















