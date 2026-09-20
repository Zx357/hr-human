/**
 * 姓名首字分组工具(通讯录 / 发起群聊 共用)
 *
 * 小程序端无拼音库/GB2312 编码接口,采用常用姓氏表覆盖(约1000字),
 * 命中映射表返回字母分组,未命中的汉字与符号归入 '#'。
 */

// 按字母组织的常用字表(以姓名首字命中为准)
const SURNAME_GROUPS = {
  C: '陈蔡曹崔程柴岑常昌车成迟仇褚丛从晁苍',
  D: '邓丁董杜戴窦段党东都单刁独达代丹德狄顿',
  E: '鄂尔而',
  F: '冯付范方费丰封符傅伏扶福樊繁房',
  G: '高郭葛顾关龚古谷甘耿桂戈盖国公官广过光归淦',
  H: '何黄胡韩郝侯洪华霍花惠贺呼哈海汉杭合和衡恒弘扈滑化皇煌回会',
  J: '蒋金贾姜江纪季简焦解靳经景巨吉籍姬嵇及郏荆计居敬竞',
  K: '孔柯康况寇库开凯亢可空',
  L: '李林刘罗梁卢雷黎骆郎吕蓝练廖凌柳龙楼陆路伦芦鲁逯赖兰岚澜乐蕾磊冷里丽励莉联廉良辽临琳玲灵令琉榴隆娄泸露栾滦仑轮沦纶萝逻洛泺漯雒',
  M: '马毛莫孟缪麦梅蒙米苗明牟木慕满迈曼茂眉枚湄美萌梦弥谧密棉淼敏鸣茗铭谟墨母沐穆暮',
  N: '倪聂牛宁南那纳耐男楠妮泥尼霓年念娘农诺暖',
  O: '欧区讴',
  P: '潘彭皮平庞裴沛朋批品聘丕篇飘苹凭萍坡泼破魄粕蒲浦圃普谱',
  Q: '钱秦齐邱乔祁强亓洽千迁佥乾潜谦羌桥侨樵巧俏切且妾怯窃亲侵钦芹琴勤青卿轻倾清晴顷庆穹丘秋求球曲屈蛆趋渠取娶去趣圈全权泉拳犬券劝缺却确雀群逡',
  R: '任阮冉荣茹芮让饶惹人仁壬刃仞认日戎茸容柔如儒汝入软锐瑞润若弱',
  S: '孙宋沈施苏石史司邵沙申尚盛师时佘舒束水税顺说硕四松嵩送诵搜粟隋岁隼唆索琐锁桑商赏上少舍深神胜圣诗十什世仕市式事侍势视试收手守首寿受书殊抒输蔬暑黍属术树竖帅双谁舜思斯私颂速宿塑算虽随损缩所',
  T: '邰唐汤陶谭田童泰坛昙檀谈坦叹棠塘膛螳倘淌躺烫趟掏涛滔绦萄逃洮桃讨套特腾誊梯剔踢锑提题蹄啼体替嚏悌逖天添甜填恬挑条跳铁帖厅汀佟彤同桐铜捅统痛偷头透突图徒途涂屠土吐兔团推腿退吞屯臀托拖脱驮鸵拓沱',
  W: '王吴魏万汪韦温伍卫文闻乌武午舞务雾完顽挽晚皖惋婉亡网往忘旺望危威微为违围帷惟维伟伪尾纬委未位味畏胃尉慰瘟纹蚊稳问翁涡窝我卧握沃无毋五侮勿物误悟',
  X: '徐许谢萧肖夏熊席向项辛邢幸匈兄雄修秀绣袖序叙绪续宣玄选炫学薛雪血寻巡旬讯迅西吸希析息悉惜稀熙喜戏系细霞下先纤掀鲜闲贤弦咸显险现献县线限相香箱详祥翔享响想象像橡消宵涵小晓孝效校些协邪胁斜谐写泄卸屑芯锌新心信兴星刑行形型醒杏姓凶胸休朽墟戌需虚须絮蓄悬旋逊',
  Y: '压押鸦杨叶严姚余阎燕尹袁岳于应尤阳央秧羊扬洋仰养样要耶也冶野业页一医衣依仪宜姨移遗疑乙已以蚁倚义亿忆艺议亦异阴音银引隐印英樱鹰迎赢影映硬拥佣永泳咏勇用优忧由邮犹游有友右幼淤鱼娱渔愉榆虞愚与宇羽雨语玉育郁预驭鸳渊元员园原圆援缘源远怨院愿约月悦阅跃越云匀允运韵蕴颜闫',
  Z: '张赵周郑朱曾翟詹章长昭折哲者锗真甄震镇正之支只旨志制治中忠钟仲重洲珠诸竹烛主祝著卓子自宗邹祖左佐战展占湛涨掌丈招兆照遮浙珍斟砧针枕诊振直值职执纸挚掷致智秩置终冢众昼皱骤住助注贮驻撰壮状撞追准捉浊兹资滋紫字综总纵走奏租足族阻组钻醉尊遵昨臧泽',
}

// 懒构建的字 -> 字母 映射
let charMap = null

function getCharMap() {
  if (!charMap) {
    charMap = new Map()
    Object.keys(SURNAME_GROUPS).forEach((letter) => {
      for (const ch of SURNAME_GROUPS[letter]) {
        if (!charMap.has(ch)) charMap.set(ch, letter)
      }
    })
  }
  return charMap
}

/**
 * 取姓名的索引字母:字母原样大写;命中常用字表的汉字返回对应字母;其余归 '#'
 * @param {string} name 姓名
 * @returns {string} A-Z 或 '#'
 */
export function getFirstLetter(name = '') {
  const first = String(name).trim().charAt(0)
  if (!first) return '#'
  if (/[a-zA-Z]/.test(first)) return first.toUpperCase()
  return getCharMap().get(first) || '#'
}

/**
 * 按索引字母分组:'#' 固定排最后,分组键为小写字母(与 tn-index-list 数据结构一致)
 * @param {Array<{username: string}>} list 联系人列表
 * @returns {Object} { a: { title: 'A', data: [...] }, ... }
 */
export function groupContacts(list) {
  const groups = {}
  list.forEach((item) => {
    const key = getFirstLetter(item.username)
    if (!groups[key]) groups[key] = { title: key, data: [] }
    groups[key].data.push(item)
  })

  return Object.keys(groups)
    .sort((a, b) => {
      if (a === '#') return 1
      if (b === '#') return -1
      return a.localeCompare(b)
    })
    .reduce((result, key) => {
      result[key.toLowerCase()] = groups[key]
      return result
    }, {})
}
