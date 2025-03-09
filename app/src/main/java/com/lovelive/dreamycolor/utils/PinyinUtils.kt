package com.lovelive.dreamycolor.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

object PinyinUtils {
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_MARK
    }

    // 预定义的日语名到罗马音的映射表
    private val japaneseRomajiMap = mapOf(
        // μ's
        "高坂 穂乃果" to "Kousaka Honoka",
        "南 ことり" to "Minami Kotori",
        "園田 海未" to "Sonoda Umi",
        "絢瀬 絵里" to "Ayase Eli",
        "東條 希" to "Toujo Nozomi",
        "矢澤 にこ" to "Yazawa Nico",
        "西木野 真姫" to "Nishikino Maki",
        "小泉 花陽" to "Koizumi Hanayo",
        "星空 凛" to "Hoshizora Rin",

        // Aqours
        "高海 千歌" to "Takami Chika",
        "桜内 梨子" to "Sakurauchi Riko",
        "渡辺 曜" to "Watanabe You",
        "松浦 果南" to "Matsuura Kanan",
        "黒澤 ダイヤ" to "Kurosawa Dia",
        "小原 鞠莉" to "Ohara Mari",
        "津島 善子" to "Tsushima Yoshiko",
        "国木田 花丸" to "Kunikida Hanamaru",
        "黒澤 ルビィ" to "Kurosawa Ruby",

        // 虹咲学园
        "高咲 侑" to "Takasaki Yuu",
        "上原 歩夢" to "Uehara Ayumu",
        "中須 かすみ" to "Nakasu Kasumi",
        "桜坂 しずく" to "Osaka Shizuku",
        "朝香 果林" to "Asaka Karin",
        "宮下 愛" to "Miyashita Ai",
        "近江 彼方" to "Oumi Kanata",
        "エマ・ヴェルデ" to "Emma Verde",
        "天王寺 璃奈" to "Tennoji Rina",
        "三船 栞子" to "Mifune Shioriko",
        "鐘 嵐珠" to "Zhong Lanzhu",
        "ミア・テイラー" to "Mia Taylor",
        "優木 せつ菜" to "Yuuki Setsuna",

        // Liella!
        "澁谷 かのん" to "Shibuya Kanon",
        "唐 可可" to "Tang Keke",
        "嵐 千砂都" to "Arashi Chisato",
        "平安名 すみれ" to "Heanna Sumire",
        "葉月 恋" to "Hazuki Ren",
        "桜小路 きな子" to "Sakurakoji Kinako",
        "米女 メイ" to "Yoneme Mei",
        "若菜 四季" to "Wakana Shiki",
        "鬼塚 夏美" to "Onitsuka Natsumi",
        "鬼塚 冬毬" to "Onitsuka Tomari",
        "ウィーン・マルガレーテ" to "Wien Margarete",

        // Hasunosora
        "日野下 花帆" to "Hinoshita Kaho",
        "村野 さやか" to "Murano Sayaka",
        "乙宗 梢" to "Otomune Kozue",
        "夕霧 綴理" to "Yukiri Tsuzuri",
        "大沢 瑠璃乃" to "Osawa Rurino",
        "藤島 慈" to "Fujishima Megumi",
        "百生 吟子" to "Momoi Ginko",
        "徒町 小鈴" to "Kachoumachi Kosuzu",
        "安養寺 姫芽" to "Anyoji Himege"
    )

    private val voiceActorRomajiMap = mapOf(
        // μ's声优
        "新田 恵海" to "Nitta Emi",
        "内田 彩" to "Uchida Aya",
        "三森 すずこ" to "Mimori Suzuko",
        "南條 愛乃" to "Nanjo Yoshino",
        "楠田 亜衣奈" to "Kusuda Aina",
        "徳井 青空" to "Tokui Sora",
        "Pile" to "Pile",
        "久保 ユリカ" to "Kubo Yurika",
        "飯田 里穂" to "Iida Riho",

        // Aqours声优
        "伊波 杏樹" to "Inami Anju",
        "逢田 梨香子" to "Aida Rikako",
        "斉藤 朱夏" to "Saito Shuka",
        "諏訪 ななか" to "Suwa Nanaka",
        "小宮 有紗" to "Komiya Arisa",
        "鈴木 愛奈" to "Suzuki Aina",
        "小林 愛香" to "Kobayashi Aika",
        "高槻 かなこ" to "Takatsuki Kanako",
        "降幡 愛" to "Furihata Ai",

        // 虹咲声优
        "矢野 妃菜喜" to "Yano Hinaki",
        "大西 亜玖璃" to "Onishi Aguri",
        "相良 茉優" to "Sagara Mayu",
        "前田 佳織里" to "Maeda Kaori",
        "久保田 未夢" to "Kubota Miyu",
        "村上 奈津実" to "Murakami Natsumi",
        "鬼頭 明里" to "Kito Akari",
        "指出 毬亜" to "Sashide Maria",
        "田中 ちえ美" to "Tanaka Chiemi",
        "小泉 萌香" to "Koizumi Moeka",
        "法元 明菜" to "Houmoto Akina",
        "内田 秀" to "Uchida Shu",
        "楠木 ともり" to "Kusunoki Tomori",
        "林 鼓子" to "Hayashi Koko",

        // Liella!声优
        "伊達 さゆり" to "Date Sayuri",
        "Liyuu" to "Liyuu",
        "岬 なこ" to "Misaki Nako",
        "ペイトン 尚未" to "Payton Naomi",
        "青山 なぎさ" to "Aoyama Nagisa",
        "鈴原 希実" to "Suzuhara Nozomi",
        "薮島 朱音" to "Yabushima Akane",
        "大熊 和奏" to "Okuma Wakana",
        "絵森 彩" to "Emori Aya",
        "坂倉 花" to "Sakakura Hana",
        "結那" to "Yuna",

        // 莲之空声优
        "楡井 希実" to "Nirei Nozomi",
        "野中 ここな" to "Nonaka Kokona",
        "花宮 初奈" to "Hanamiya Hatsuna",
        "佐々木 琴子" to "Sasaki Kotoko",
        "菅 叶和" to "Suga Kanna",
        "月音 こな" to "Tsukioto Kona",
        "櫻井 陽菜" to "Sakurai Haruna",
        "葉山 風花" to "Hayama Fuuka",
        "来栖 りん" to "Kurusu Rin"
    )

    // 角色中文名到拼音的映射表
    val chinesePinyinMap = mapOf(
        // μ's 角色
        "高坂穗乃果" to "Gāo Bǎn Suì Nǎi Guǒ",
        "南小鸟" to "Nán Xiǎo Niǎo",
        "园田海未" to "Yuán Tián Hǎi Wèi",
        "绚濑绘里" to "Xuàn Lài Huì Lǐ",
        "东条希" to "Dōng Tiáo Xī",
        "矢泽妮可" to "Shǐ Zé Nī Kě",
        "西木野真姬" to "Xī Mù Yě Zhēn Jī",
        "小泉花阳" to "Xiǎo Quán Huā Yáng",
        "星空凛" to "Xīng Kōng Lín",

        // Aqours 角色
        "高海千歌" to "Gāo Hǎi Qiān Gē",
        "樱内梨子" to "Yīng Nèi Lí Zǐ",
        "渡边曜" to "Dù Biān Yào",
        "松浦果南" to "Sōng Pǔ Guǒ Nán",
        "黑泽黛雅" to "Hēi Zé Dài Yǎ",
        "小原鞠莉" to "Xiǎo Yuán Jū Lì",
        "津岛善子" to "Jīn Dǎo Shàn Zǐ",
        "国木田花丸" to "Guó Mù Tián Huā Wán",
        "黑泽露比" to "Hēi Zé Lù Bǐ",

        // 虹咲学园 角色
        "高咲侑" to "Gāo Xiào Yòu",
        "上原步梦" to "Shàng Yuán Bù Mèng",
        "中须霞" to "Zhōng Xū Xiá",
        "樱坂雫" to "Yīng Bǎn Nǎ",
        "朝香果林" to "Cháo Xiāng Guǒ Lín",
        "宫下爱" to "Gōng Xià Ài",
        "近江彼方" to "Jìn Jiāng Bǐ Fāng",
        "艾玛·维尔德" to "Ài Mǎ Wéi Ěr Dé",
        "天王寺璃奈" to "Tiān Wáng Sì Lí Nài",
        "三船栞子" to "Sān Chuán Kān Zǐ",
        "钟岚珠" to "Zhōng Lán Zhū",
        "米雅·泰勒" to "Mǐ Yǎ Tài Lè",
        "优木雪菜" to "Yōu Mù Xuě Cài",

        // Liella! 角色
        "涩谷香音" to "Sè Gǔ Xiāng Yīn",
        "唐可可" to "Táng Kě Kě",
        "岚千砂都" to "Lán Qiān Shā Dōu",
        "平安名堇" to "Píng Ān Míng Jǐn",
        "叶月恋" to "Yè Yuè Liàn",
        "樱小路希奈子" to "Yīng Xiǎo Lù Xī Nài Zǐ",
        "米女芽衣" to "Mǐ Nǚ Yá Yī",
        "若菜四季" to "Ruò Cài Sì Jì",
        "鬼塚夏美" to "Guǐ Zhuāng Xià Měi",
        "鬼塚冬毬" to "Guǐ Zhuāng Dōng Qiú",
        "薇恩·玛格丽特" to "Wēi Ēn Mǎ Gé Lì Tè",

        // 莲之空 角色
        "日野下花帆" to "Rì Yě Xià Huā Fān",
        "村野沙耶香" to "Cūn Yě Shā Yē Xiāng",
        "乙宗梢" to "Yǐ Zōng Shāo",
        "夕雾缀理" to "Xī Wù Zhuì Lǐ",
        "大泽瑠璃乃" to "Dà Zé Liú Lí Nǎi",
        "藤岛慈" to "Téng Dǎo Cí",
        "百生吟子" to "Bǎi Shēng Yín Zǐ",
        "徒町小铃" to "Tú Dīng Xiǎo Líng",
        "安养寺姬芽" to "Ān Yǎng Sì Jī Yá",

        // μ's 声优
        "新田惠海" to "Xīn Tián Huì Hǎi",
        "内田彩" to "Nèi Tián Cǎi",
        "三森铃子" to "Sān Sēn Líng Zǐ",
        "南条爱乃" to "Nán Tiáo Ài Nǎi",
        "楠田亚衣奈" to "Nán Tián Yà Yī Nài",
        "德井青空" to "Dé Jǐng Qīng Kōng",
        "久保由利香" to "Jiǔ Bǎo Yóu Lì Xiāng",
        "Pile" to "Pile",
        "饭田里穗" to "Fàn Tián Lǐ Suì",

        // Aqours 声优
        "伊波杏树" to "Yī Bō Xìng Shù",
        "逢田梨香子" to "Féng Tián Lí Xiāng Zǐ",
        "齐藤朱夏" to "Qí Téng Zhū Xià",
        "诹访奈奈香" to "Zōu Fǎng Nài Nài Xiāng",
        "小宫有纱" to "Xiǎo Gōng Yǒu Shā",
        "铃木爱奈" to "Líng Mù Ài Nài",
        "小林爱香" to "Xiǎo Lín Ài Xiāng",
        "高槻加奈子" to "Gāo Jú Jiā Nài Zǐ",
        "降幡爱" to "Jiàng Fān Ài",

        // 虹咲 声优
        "矢野妃菜喜" to "Shǐ Yě Fēi Cài Xǐ",
        "大西亚玖璃" to "Dà Xī Yà Jiǔ Lí",
        "相良茉优" to "Xiāng Liáng Mò Yōu",
        "前田佳织里" to "Qián Tián Jiā Zhī Lǐ",
        "久保田未梦" to "Jiǔ Bǎo Tián Wèi Mèng",
        "村上奈津实" to "Cūn Shàng Nài Jīn Shí",
        "鬼头明里" to "Guǐ Tóu Míng Lǐ",
        "指出毬亚" to "Zhǐ Chū Qiú Yà",
        "田中千惠美" to "Tián Zhōng Qiān Huì Měi",
        "小泉萌香" to "Xiǎo Quán Méng Xiāng",
        "法元明菜" to "Fǎ Yuán Míng Cài",
        "内田秀" to "Nèi Tián Xiù",
        "楠木灯" to "Nán Mù Dēng",
        "林鼓子" to "Lín Gǔ Zǐ",

        // Liella! 声优
        "伊达小百合" to "Yī Dá Xiǎo Bǎi Hé",
        "Liyuu" to "Liyuu",
        "Payton尚未" to "Payton Shàng Wèi",
        "岬奈子" to "Jiǎ Nài Zǐ",
        "青山渚" to "Qīng Shān Nǎ",
        "铃原希实" to "Líng Yuán Xī Shí",
        "薮岛朱音" to "Yǎo Dǎo Zhū Yīn",
        "大熊和奏" to "Dà Xióng Hé Zòu",
        "绘森彩" to "Huì Sēn Cǎi",
        "坂仓花" to "Bǎn Cāng Huā",
        "结那" to "Jié Nà",

        // 莲之空 声优
        "榆井希实" to "Yú Jǐng Xī Shí",
        "野中心菜" to "Yě Zhōng Xīn Cài",
        "花宫初奈" to "Huā Gōng Chū Nài",
        "佐佐木琴子" to "Zuǒ Zuǒ Mù Qín Zǐ",
        "菅叶和" to "Jiān Yè Hé",
        "月音粉" to "Yuè Yīn Fěn",
        "樱井阳菜" to "Yīng Jǐng Yáng Cài",
        "叶山风花" to "Yè Shān Fēng Huā",
        "来栖凛" to "Lái Qī Lín"
    )


    // 转换中文名字为带拼音格式
    fun addPinyinToChinese(name: String): String {
        // 使用映射表查找
        chinesePinyinMap[name]?.let {
            return "$name ($it)"
        }

        // 如果没有找到映射，则使用原实现方法
        val sb = StringBuilder()
        for (char in name) {
            if (char.toString().matches("[\u4E00-\u9FA5]".toRegex())) {
                try {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                    if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                        val pinyin = pinyinArray[0]
                        sb.append(char).append("(").append(pinyin).append(")")
                    } else {
                        sb.append(char)
                    }
                } catch (e: BadHanyuPinyinOutputFormatCombination) {
                    sb.append(char)
                }
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }
    // 转换日文名为罗马拼音
    fun convertJapaneseToRomaji(japaneseName: String): String {
        // 先检查是否是角色名
        japaneseRomajiMap[japaneseName]?.let { return it }

        // 再检查是否是声优名
        voiceActorRomajiMap[japaneseName]?.let { return it }

        // 如果都找不到，返回原名加注释
        return "$japaneseName (romaji)"
    }
}