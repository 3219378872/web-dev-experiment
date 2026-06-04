/* 好集 HAOJI — C端共享头部/分类导航/页脚注入
   用法：在 <body> 顶部放 <div id="app-header"></div>，底部放 <div id="app-footer"></div>
   可在 <body data-cart="3" data-page="home"> 上设置购物车数与当前页 */
(function(){
  var cats = ['手机数码','家用电器','服饰鞋包','美妆个护','食品生鲜','家居家装','母婴玩具','运动户外','图书文娱','汽车用品'];
  var body = document.body;
  var cart = body.getAttribute('data-cart') || '3';
  var logged = body.getAttribute('data-logged') !== 'false';

  var topbar =
   '<div class="topbar"><div class="wrap">'+
     '<div>'+ (logged
        ? '你好，<a href="profile.html">林晓</a><span class="sep">|</span><a href="orders.html">我的订单</a>'
        : '<a href="login.html">请登录</a><span class="sep">|</span><a href="register.html">免费注册</a>')+
     '</div>'+
     '<div>'+
       '<a href="orders.html">我的订单</a><span class="sep">|</span>'+
       '<a href="favorites.html">收藏夹</a><span class="sep">|</span>'+
       '<a href="coupons.html">优惠券</a><span class="sep">|</span>'+
       '<a href="announcements.html">公告</a><span class="sep">|</span>'+
       '<a href="service.html">客服</a><span class="sep">|</span>'+
       '<a href="../admin/login.html">商家后台</a>'+
     '</div>'+
   '</div></div>';

  var header =
   '<div class="header"><div class="wrap">'+
     '<a class="logo" href="index.html"><span class="mark">集</span>'+
       '<span class="name">好集<small>HAOJI MALL</small></span></a>'+
     '<div style="flex:1;max-width:560px;">'+
       '<form class="searchbar" onsubmit="location.href=\'search.html\';return false;">'+
         '<input placeholder="搜索 你想要的好物～" value="">'+
         '<button type="submit">🔍 搜索</button>'+
       '</form>'+
       '<div class="hot-words"><span class="dim">热搜：</span>'+
         '<a href="search.html">蓝牙耳机</a><a href="search.html">空气炸锅</a>'+
         '<a href="search.html">通勤包</a><a href="search.html" style="color:var(--price)">秒杀</a></div>'+
     '</div>'+
     '<div class="header-actions">'+
       '<a class="icon-btn" href="favorites.html"><span class="ic">♡</span>收藏</a>'+
       '<a class="icon-btn" href="orders.html"><span class="ic">▤</span>订单</a>'+
       '<a class="icon-btn cart-btn" href="cart.html"><span class="ic">🛒</span>购物车'+
         '<span class="badge">'+cart+'</span></a>'+
       '<a class="icon-btn" href="profile.html"><span class="ic">◍</span>我的</a>'+
     '</div>'+
   '</div></div>';

  var page = body.getAttribute('data-page');
  var navmap = {phone:'手机数码',appliance:'家用电器',fashion:'服饰鞋包'};
  var catnav =
   '<div class="catnav"><div class="wrap">'+
     '<a class="all" href="category.html">▦ 全部商品分类</a>'+
     cats.slice(0,8).map(function(c){
        return '<a href="category.html"'+(navmap[page]===c?' style="color:var(--brand);font-weight:700"':'')+'>'+c+'</a>';
     }).join('')+
     '<span class="spacer"></span>'+
     '<a class="promo" href="flashsale.html">⚡ 限时秒杀</a>'+
     '<a href="coupons.html">领券中心</a>'+
   '</div></div>';

  var footer =
   '<footer class="footer"><div class="wrap">'+
     '<div class="cols">'+
       '<div>'+
         '<a class="logo" href="index.html" style="margin-bottom:12px"><span class="mark">集</span>'+
           '<span class="name">好集<small>HAOJI MALL</small></span></a>'+
         '<p class="muted" style="font-size:12.5px;max-width:280px">万物好集 · 一站式综合百货商城。正品保障，极速配送，七天无理由退换。</p>'+
         '<div class="trust">'+
           '<div class="it"><span class="d">✓</span>正品保障</div>'+
           '<div class="it"><span class="d">⚡</span>极速发货</div>'+
           '<div class="it"><span class="d">↺</span>七天退换</div>'+
         '</div>'+
       '</div>'+
       footCol('购物指南',['新手上路','支付方式','配送服务','售后保障']) +
       footCol('账户服务',['注册登录','个人信息','我的订单','收货地址']) +
       footCol('特色服务',['限时秒杀','领券中心','会员中心','在线客服']) +
       footCol('关于好集',['关于我们','加入我们','商家入驻','联系方式']) +
     '</div>'+
     '<div class="copy">© 2026 好集 HAOJI MALL · 综合百货电商平台 · 沪ICP备 2026000000 号 · 本页面为高保真设计原型，商品图以色块占位</div>'+
   '</div></footer>';

  function footCol(t,items){
    return '<div><h4>'+t+'</h4><ul>'+items.map(function(i){return '<li><a href="#">'+i+'</a></li>';}).join('')+'</ul></div>';
  }

  // 账户中心侧栏
  var acct = body.getAttribute('data-acct');
  var acctGroups=[
    ['交易管理',[['orders.html','我的订单','▤'],['order-detail.html','订单详情','📦'],['cart.html','购物车','🛒'],['favorites.html','我的收藏','♡']]],
    ['营销中心',[['coupons.html','优惠券','🎟'],['flashsale.html','限时秒杀','⚡']]],
    ['账户设置',[['profile.html','个人信息','◐'],['address.html','收货地址','📍'],['feedback.html','意见反馈','✍'],['service.html','在线客服','💬']]]
  ];
  var acctHtml='<div class="acct-side">'+
    '<div class="acct-user"><div class="av">林</div><div><b>林晓</b><span class="lv">普通会员 · 积分1280</span></div></div>'+
    acctGroups.map(function(g){
      return '<div class="acct-grp"><div class="gt">'+g[0]+'</div>'+
        g[1].map(function(it){
          var on = acct===it[0];
          return '<a href="'+it[0]+'" class="'+(on?'on':'')+'"><span class="i">'+it[2]+'</span>'+it[1]+'</a>';
        }).join('')+'</div>';
    }).join('')+'</div>';

  function set(id,html){var el=document.getElementById(id); if(el) el.outerHTML=html;}
  set('app-account', acctHtml);
  set('app-topbar', topbar);
  set('app-header', header);
  set('app-catnav', catnav);
  set('app-footer', footer);
})();
