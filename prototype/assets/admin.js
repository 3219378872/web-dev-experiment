/* 好集后台 — 共享侧栏 + 顶栏注入
   用法：<body class="admin" data-adm="dashboard"> 含 <div id="adm-side"></div> 与 <div id="adm-top" data-crumb="..."></div> */
(function(){
  var nav=[
    ['总览',[['dashboard','数据看板','📊',''],]],
    ['运营',[
      ['products','商品管理','📦',''],
      ['categories','分类管理','🗂',''],
      ['orders','订单管理','▤','5'],
      ['reviews','评价管理','★',''],
      ['users','用户管理','👥','']
    ]],
    ['内容',[
      ['banners','轮播管理','🖼',''],
      ['announcements','公告管理','📢',''],
      ['feedback','反馈管理','💬','3']
    ]],
    ['系统',[
      ['settings','个人中心','⚙','']
    ]]
  ];
  var active = document.body.getAttribute('data-adm');
  var sideHtml =
   '<div class="adm-brand"><span class="mark">集</span><span class="nm">好集后台<small>HAOJI ADMIN</small></span></div>'+
   '<nav class="adm-nav">'+
     nav.map(function(g){
       return '<div class="grp-t">'+g[0]+'</div>'+
         g[1].map(function(it){
           var on = active===it[0];
           return '<a href="'+it[0]+'.html" class="'+(on?'on':'')+'"><span class="i">'+it[2]+'</span>'+it[1]+
             (it[3]?'<span class="badge">'+it[3]+'</span>':'')+'</a>';
         }).join('');
     }).join('')+
   '</nav>'+
   '<div class="foot">好集 HAOJI ADMIN v2.6<br>© 2026 高保真原型</div>';

  var crumb = (document.getElementById('adm-top')||{}).getAttribute ? document.getElementById('adm-top').getAttribute('data-crumb') : '';
  var topHtml =
   '<div class="crumb">好集后台 <span style="margin:0 6px;color:var(--line-2)">/</span> <b>'+(crumb||'数据看板')+'</b></div>'+
   '<div class="search">🔍 搜索订单、商品、用户…</div>'+
   '<div class="tools">'+
     '<a class="tbtn" href="feedback.html" title="消息"><span>✉</span><span class="dot"></span></a>'+
     '<a class="tbtn" href="announcements.html" title="通知"><span>🔔</span><span class="dot"></span></a>'+
     '<a class="tbtn" href="../c/index.html" title="前台" target="_blank"><span>🏪</span></a>'+
     '<div class="me"><span class="av">管</span><div class="nm">管理员 Admin<small>超级管理员</small></div></div>'+
   '</div>';

  function set(id,html,cls){var el=document.getElementById(id); if(el){el.innerHTML=html; if(cls) el.className=cls;}}
  set('adm-side', sideHtml, 'adm-side');
  set('adm-top', topHtml, 'adm-top');
})();
