/* 好集 HAOJI — 共享商品数据 + 卡片渲染 */
window.HAOJI = (function(){
  var glyphs={数码:'▣',家电:'▤',服饰:'◈',美妆:'✿',食品:'◉',家居:'▦',母婴:'❀',运动:'◐',图书:'▥'};
  // 24 products
  var P = [
    {id:1, n:'TWS 主动降噪蓝牙耳机 入耳式 长续航', p:299, o:499, sales:8421, s:'s4', g:'数码', cat:'手机数码', tag:'热卖', shop:'好集数码官方旗舰店'},
    {id:2, n:'超大容量空气炸锅 家用多功能无油低脂', p:269, o:399, sales:12930, s:'s1', g:'家电', cat:'家用电器', tag:'秒杀', shop:'好集家电自营'},
    {id:3, n:'轻商务通勤双肩背包 防泼水大容量电脑包', p:159, o:259, sales:5240, s:'s3', g:'服饰', cat:'服饰鞋包', tag:'新品', shop:'集货优选'},
    {id:4, n:'氨基酸温和洁面乳 深层清洁 男女通用', p:69, o:99, sales:23110, s:'s6', g:'美妆', cat:'美妆个护', tag:'回购', shop:'好集美妆馆'},
    {id:5, n:'每日坚果混合礼盒 750g 30小包独立装', p:89, o:139, sales:18760, s:'s2', g:'食品', cat:'食品生鲜', tag:'热卖', shop:'好集食品旗舰店'},
    {id:6, n:'北欧实木收纳边几 客厅沙发角小桌', p:199, o:329, sales:3120, s:'s8', g:'家居', cat:'家居家装', tag:'新品', shop:'木集家居'},
    {id:7, n:'婴儿纯棉连体哈衣 新生儿和尚服 3件装', p:99, o:159, sales:9430, s:'s5', g:'母婴', cat:'母婴玩具', tag:'热卖', shop:'好集母婴'},
    {id:8, n:'专业跑步运动鞋 轻量缓震透气网面', p:349, o:599, sales:6720, s:'s7', g:'运动', cat:'运动户外', tag:'促销', shop:'动集运动'},
    {id:9, n:'4K 高清智能投影仪 家用卧室便携', p:1299, o:1999, sales:2140, s:'s4', g:'数码', cat:'手机数码', tag:'秒杀', shop:'好集数码官方旗舰店'},
    {id:10, n:'轻奢真皮单肩斜挎女包 通勤百搭', p:459, o:899, sales:4310, s:'s6', g:'服饰', cat:'服饰鞋包', tag:'新品', shop:'集货优选'},
    {id:11, n:'破壁料理机 家用加热静音榨汁豆浆机', p:399, o:699, sales:7820, s:'s1', g:'家电', cat:'家用电器', tag:'热卖', shop:'好集家电自营'},
    {id:12, n:'高保湿烟酰胺面部精华液 30ml 提亮肤色', p:129, o:199, sales:15600, s:'s5', g:'美妆', cat:'美妆个护', tag:'回购', shop:'好集美妆馆'},
    {id:13, n:'有机原切牛排套餐 10片装 谷饲眼肉', p:199, o:328, sales:11200, s:'s6', g:'食品', cat:'食品生鲜', tag:'促销', shop:'好集食品旗舰店'},
    {id:14, n:'记忆棉人体工学办公椅 久坐护腰电脑椅', p:599, o:1099, sales:3980, s:'s4', g:'家居', cat:'家居家装', tag:'热卖', shop:'木集家居'},
    {id:15, n:'儿童积木益智拼装玩具 1000颗粒大盒装', p:139, o:229, sales:8650, s:'s2', g:'母婴', cat:'母婴玩具', tag:'新品', shop:'好集母婴'},
    {id:16, n:'碳纤维超轻折叠登山徒步杖 一对装', p:129, o:219, sales:2470, s:'s3', g:'运动', cat:'运动户外', tag:'促销', shop:'动集运动'},
    {id:17, n:'机械键盘 客制化热插拔 RGB 游戏办公', p:329, o:499, sales:6140, s:'s4', g:'数码', cat:'手机数码', tag:'新品', shop:'好集数码官方旗舰店'},
    {id:18, n:'24英寸 2K 高清护眼显示器 窄边框', p:799, o:1199, sales:4520, s:'s7', g:'数码', cat:'手机数码', tag:'热卖', shop:'好集数码官方旗舰店'},
    {id:19, n:'纯棉四件套 60支长绒棉 裸睡级床品', p:289, o:499, sales:9870, s:'s5', g:'家居', cat:'家居家装', tag:'回购', shop:'木集家居'},
    {id:20, n:'电动升降桌 站立办公 双电机静音', p:899, o:1599, sales:1820, s:'s8', g:'家居', cat:'家居家装', tag:'秒杀', shop:'木集家居'},
    {id:21, n:'保温焖烧杯 316不锈钢 大容量便携', p:79, o:139, sales:14300, s:'s7', g:'家居', cat:'家居家装', tag:'热卖', shop:'好集家电自营'},
    {id:22, n:'男士轻薄羽绒服 90白鸭绒 修身保暖', p:399, o:799, sales:5310, s:'s4', g:'服饰', cat:'服饰鞋包', tag:'促销', shop:'集货优选'},
    {id:23, n:'智能手表 血氧心率监测 多运动模式', p:259, o:459, sales:10240, s:'s4', g:'数码', cat:'手机数码', tag:'秒杀', shop:'好集数码官方旗舰店'},
    {id:24, n:'进口冻干猫粮 全阶段无谷高蛋白 2kg', p:189, o:299, sales:7600, s:'s2', g:'食品', cat:'食品生鲜', tag:'新品', shop:'萌宠集'}
  ];
  var byId=function(id){return P.filter(function(x){return x.id==id;})[0];};

  var tagClass={'热卖':'tag-price','秒杀':'tag-price','新品':'tag-new','促销':'tag-gold','回购':'tag-success'};

  function phBlock(p, extra){
    var glyph = {数码:'▣',家电:'▤',服饰:'◈',美妆:'✿',食品:'◉',家居:'▦',母婴:'❀',运动:'◐'}[p.g]||'▦';
    return '<div class="ph '+p.s+'" data-label="'+p.cat+'" '+(extra||'')+'>'+
           '<span class="glyph">'+glyph+'</span></div>';
  }

  function card(p){
    var tc = tagClass[p.tag]||'tag-ghost';
    return '<a class="pcard" href="product.html?id='+p.id+'">'+
      phBlock(p)+
      '<div class="body">'+
        '<div class="tags"><span class="tag '+tc+'">'+p.tag+'</span>'+
          (p.o>p.p?'<span class="tag tag-line">省¥'+(p.o-p.p)+'</span>':'')+'</div>'+
        '<div class="title">'+p.n+'</div>'+
        '<div class="row"><span class="price" style="font-size:19px"><span class="cur">¥</span>'+p.p+'</span>'+
          (p.o>p.p?'<span class="price-old">¥'+p.o+'</span>':'')+'</div>'+
        '<div class="meta"><span>已售 '+ (p.sales>9999?(p.sales/10000).toFixed(1)+'万':p.sales) +'</span>'+
          '<span class="dim">'+p.shop.slice(0,6)+'</span></div>'+
      '</div></a>';
  }

  function renderGrid(sel, list){
    var el = typeof sel==='string'?document.querySelector(sel):sel;
    if(el) el.innerHTML = list.map(card).join('');
  }

  return {P:P, byId:byId, card:card, renderGrid:renderGrid, phBlock:phBlock, tagClass:tagClass};
})();
