<template>
  <footer class="footer">
    <div class="wrap cols">
      <div>
        <router-link
          to="/"
          class="logo"
          style="margin-bottom: 16px; display: flex; align-items: center; gap: 10px"
        >
          <span
            class="mark"
            style="
              width: 36px;
              height: 36px;
              border-radius: 10px;
              background: linear-gradient(135deg, var(--brand), var(--brand-700));
              display: grid;
              place-items: center;
              color: #fff;
              font-weight: 900;
              font-size: 18px;
            "
            >集</span
          >
          <span style="font-weight: 900; font-size: 18px; color: var(--ink)"
            >好集<small
              style="
                display: block;
                font-size: 9px;
                color: var(--ink-3);
                letter-spacing: 1.5px;
                font-weight: 500;
              "
              >HAOJI MALL</small
            ></span
          >
        </router-link>
        <p style="font-size: 12.5px; color: var(--ink-2); line-height: 1.7; max-width: 260px">
          综合百货电商平台，正品保障，极速发货，七天无理由退换。好集，万物好集。
        </p>
        <div class="trust" style="margin-top: 18px">
          <div class="it"><span class="d">✓</span>正品保障</div>
          <div class="it"><span class="d">⚡</span>极速发货</div>
          <div class="it"><span class="d">↺</span>七天退换</div>
        </div>
      </div>
      <div>
        <h4>购物指南</h4>
        <ul>
          <li><router-link to="/login">购物流程</router-link></li>
          <li><router-link to="/login">会员介绍</router-link></li>
          <li><router-link to="/login">常见问题</router-link></li>
          <li><router-link to="/feedback">联系客服</router-link></li>
        </ul>
      </div>
      <div>
        <h4>配送方式</h4>
        <ul>
          <li><a href="#">上门自提</a></li>
          <li><a href="#">211限时达</a></li>
          <li><a href="#">配送服务查询</a></li>
          <li><a href="#">配送费收取标准</a></li>
        </ul>
      </div>
      <div>
        <h4>支付方式</h4>
        <ul>
          <li><a href="#">货到付款</a></li>
          <li><a href="#">在线支付</a></li>
          <li><a href="#">分期付款</a></li>
          <li><a href="#">公司转账</a></li>
        </ul>
      </div>
      <div>
        <h4>售后服务</h4>
        <ul>
          <li><a href="#">售后政策</a></li>
          <li><a href="#">价格保护</a></li>
          <li><a href="#">退款说明</a></li>
          <li><router-link to="/feedback">取消订单</router-link></li>
        </ul>
      </div>
    </div>
    <div class="copy">
      <div class="wrap" style="padding-top: 18px; padding-bottom: 18px">
        © 2026 好集 HAOJI MALL · 综合百货电商平台 · 仅供课程实验演示
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: #fff;
  border-top: 1px solid var(--line);
  margin-top: 50px;
}
.footer .cols {
  display: grid;
  grid-template-columns: 1.4fr repeat(4, 1fr);
  gap: 30px;
  padding: 42px 0 30px;
}
.footer h4 {
  font-size: 13.5px;
  font-weight: 700;
  margin-bottom: 14px;
  color: var(--ink);
}
.footer ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 9px;
}
.footer a {
  font-size: 12.5px;
  color: var(--ink-2);
}
.footer a:hover {
  color: var(--brand);
}
.footer .copy {
  border-top: 1px solid var(--line);
  text-align: center;
  font-size: 12px;
  color: var(--ink-3);
}
.trust {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 10px;
}
.trust .it {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-2);
}
.trust .it .d {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--brand-softer);
  color: var(--brand);
  display: grid;
  place-items: center;
  font-size: 16px;
}
</style>
