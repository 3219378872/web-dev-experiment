<template>
  <div class="wrap" style="padding: 14px 0 40px">
    <div class="crumb">
      <router-link to="/">首页</router-link><span class="s">/</span
      ><b style="color: var(--ink)">在线客服</b>
    </div>

    <div class="chat">
      <div class="chat-side">
        <div class="agent">
          <div class="av">客<span class="on"></span></div>
          <b>好集客服 · 小集</b>
          <div class="role">在线 · 平均 30s 响应</div>
        </div>
        <div class="quick">
          <div class="qt">常见问题</div>
          <a v-for="q in quickQuestions" :key="q" @click.prevent="sendQuick(q)">{{ q }}</a>
        </div>
      </div>

      <div class="chat-main">
        <div class="chat-head">
          <span
            style="
              width: 9px;
              height: 9px;
              border-radius: 50%;
              background: var(--success);
              display: inline-block;
            "
          ></span>
          <b>好集客服 · 小集</b>
          <span class="dim" style="font-size: 12px">为您服务</span>
        </div>
        <div ref="chatBody" class="chat-body">
          <div v-for="(msg, idx) in messages" :key="idx">
            <div v-if="msg.type === 'daySep'" class="day-sep">{{ msg.text }}</div>
            <div v-else-if="msg.type === 'card'" :class="['msg', msg.me ? 'me' : '']">
              <div class="av" :style="`background:${msg.me ? '#88A6B8' : 'var(--brand)'}`">
                {{ msg.me ? '林' : '客' }}
              </div>
              <div class="card-msg">
                <div class="ph s4"><div class="shape round"></div></div>
                <div>
                  <div class="nm">{{ msg.title }}</div>
                  <div class="pr">¥{{ msg.price }}</div>
                </div>
              </div>
            </div>
            <div v-else :class="['msg', msg.me ? 'me' : '']">
              <div class="av" :style="`background:${msg.me ? '#88A6B8' : 'var(--brand)'}`">
                {{ msg.me ? '林' : '客' }}
              </div>
              <div class="bubble">{{ msg.text }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <div class="chat-tools">
            <span>😊</span><span>🖼</span><span>📎</span><span>📦 订单</span>
          </div>
          <div class="chat-send">
            <textarea
              v-model="inputText"
              rows="2"
              placeholder="请输入您的问题…"
              @keydown.enter.prevent="sendMessage"
            ></textarea>
            <button class="btn btn-primary" style="align-self: flex-end" @click="sendMessage">
              发送
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';

const inputText = ref('');
const chatBody = ref(null);

const quickQuestions = [
  '📦 我的订单什么时候发货？',
  '↺ 如何申请退货退款？',
  '🎟 优惠券怎么使用？',
  '🚚 支持哪些配送方式？',
  '💳 支持哪些支付方式？',
  '📞 人工客服热线 400-888-0099',
];

const messages = ref([
  { type: 'daySep', text: '今天 14:28' },
  { type: 'text', text: '您好，欢迎来到好集！很高兴为您服务，请问有什么可以帮您？😊', me: false },
  { type: 'text', text: '我买的蓝牙耳机什么时候能发货呀？', me: true },
  {
    type: 'text',
    text: '已为您查询到订单 202605281588001，商品为现货，将在付款后 24 小时内发出，请您放心～',
    me: false,
  },
  { type: 'card', title: 'TWS 主动降噪蓝牙耳机 入耳式', price: 299, me: false },
  { type: 'text', text: '好的，谢谢！', me: true },
]);

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight;
  });
}

function sendMessage() {
  const text = inputText.value.trim();
  if (!text) return;
  messages.value.push({ type: 'text', text, me: true });
  inputText.value = '';
  scrollToBottom();
  // TODO: integrate with real chat/notify-service WebSocket or API
  setTimeout(() => {
    messages.value.push({
      type: 'text',
      text: '收到您的问题，人工客服正在接入中，请稍候…',
      me: false,
    });
    scrollToBottom();
  }, 800);
}

function sendQuick(q) {
  messages.value.push({ type: 'text', text: q, me: true });
  scrollToBottom();
  setTimeout(() => {
    messages.value.push({
      type: 'text',
      text: '收到您的问题，人工客服正在接入中，请稍候…',
      me: false,
    });
    scrollToBottom();
  }, 600);
}
</script>

<style scoped>
.chat {
  display: grid;
  grid-template-columns: 240px 1fr;
  height: 600px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  margin: 14px 0 30px;
}
.chat-side {
  background: var(--bg);
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
}
.chat-side .agent {
  padding: 18px;
  text-align: center;
  border-bottom: 1px solid var(--line);
}
.chat-side .agent .av {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a45, var(--brand));
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 26px;
  margin: 0 auto 10px;
  position: relative;
}
.chat-side .agent .av .on {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--success);
  border: 2px solid #fff;
}
.chat-side .agent b {
  font-size: 15px;
}
.chat-side .agent .role {
  font-size: 12px;
  color: var(--ink-3);
}
.chat-side .quick {
  padding: 14px;
}
.chat-side .quick .qt {
  font-size: 12px;
  color: var(--ink-3);
  margin-bottom: 10px;
  font-weight: 700;
}
.chat-side .quick a {
  display: block;
  padding: 9px 12px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 8px;
  font-size: 12.5px;
  color: var(--ink-2);
  margin-bottom: 8px;
  cursor: pointer;
}
.chat-side .quick a:hover {
  border-color: var(--brand);
  color: var(--brand);
}
.chat-main {
  display: flex;
  flex-direction: column;
}
.chat-head {
  padding: 14px 20px;
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  gap: 10px;
}
.chat-head b {
  font-size: 15px;
}
.chat-body {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background: var(--bg);
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.msg {
  display: flex;
  gap: 10px;
  max-width: 70%;
}
.msg .av {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}
.msg .bubble {
  background: #fff;
  border-radius: 3px 12px 12px 12px;
  padding: 11px 14px;
  font-size: 13.5px;
  line-height: 1.55;
  box-shadow: var(--shadow-1);
}
.msg.me {
  margin-left: auto;
  flex-direction: row-reverse;
}
.msg.me .bubble {
  background: var(--brand);
  color: #fff;
  border-radius: 12px 3px 12px 12px;
}
.msg .time {
  font-size: 10.5px;
  color: var(--ink-3);
  text-align: center;
}
.day-sep {
  text-align: center;
  font-size: 11.5px;
  color: var(--ink-3);
}
.card-msg {
  background: #fff;
  border-radius: 10px;
  padding: 10px;
  box-shadow: var(--shadow-1);
  display: flex;
  gap: 10px;
  width: 260px;
}
.card-msg .ph {
  width: 54px;
  height: 54px;
  border-radius: 8px;
  flex-shrink: 0;
}
.card-msg .nm {
  font-size: 12.5px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-msg .pr {
  color: var(--price);
  font-weight: 900;
  font-size: 14px;
  margin-top: 4px;
}
.chat-input {
  border-top: 1px solid var(--line);
  padding: 12px 16px;
}
.chat-tools {
  display: flex;
  gap: 14px;
  color: var(--ink-3);
  font-size: 17px;
  margin-bottom: 8px;
}
.chat-tools span {
  cursor: pointer;
}
.chat-tools span:hover {
  color: var(--brand);
}
.chat-send {
  display: flex;
  gap: 10px;
}
.chat-send textarea {
  flex: 1;
  border: 1px solid var(--line-2);
  border-radius: 8px;
  padding: 10px;
  font-size: 13.5px;
  resize: none;
  outline: 0;
  font-family: inherit;
}
.chat-send textarea:focus {
  border-color: var(--brand);
  box-shadow: 0 0 0 3px var(--brand-softer);
}

@media (max-width: 768px) {
  .chat {
    grid-template-columns: 1fr;
    height: auto;
  }
  .chat-side {
    display: none;
  }
}
</style>
