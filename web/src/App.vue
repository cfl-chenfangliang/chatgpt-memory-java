<script setup lang="ts">
import { completion } from '@/api'
import useMessages from '@/composables/messages'
import { TSummary } from '@/types'

const state = reactive({
  message: '',
  loadding: false,
  visible: false,
  summary: {} as TSummary,
})
const messages = useMessages()

const sendMessage = async (event: { preventDefault: () => void }) => {
  event.preventDefault() // 阻止默认事件
  state.loadding = true
  messages.addMessage({
    username: "user",
    msg: state.message,
    type: 1,
  })
  let question = state.message
  state.message = ""
  // 这里写死拿来测试的
  const conversationId = "test1"
  const data: any =  await completion(conversationId, question)
  const replyMessage = data?.data ? (data.data) : data.message
  messages.addMessage({
    username: "chatGPT",
    msg: replyMessage,
    type: 0,
  })
  state.loadding = false
}

onMounted(async () => {
})
</script>

<template>
  <div id="layout">
    <header id="header" class="bg-dark-50 text-white h-10 select-none">
      <LoadingOutlined v-if="state.loadding" class="pl-3 cursor-pointer" />
      <span class="text-size-5 pl-5">ChatGPT-Memory-Java</span>

    </header>
    <div id="layout-body">
      <main id="main">
        <div class="flex-1 relative flex flex-col">
          <!-- header -->
          <!-- content -->
          <div class="flex-1 inset-0 overflow-hidden bg-transparent bg-bottom bg-cover flex flex-col">
            <!-- dialog -->
            <div class="flex-1 w-full self-center">
              <div class="relative px-3 py-1 m-auto flex flex-col">
                <Message :message=msg v-for="msg in messages.messages.value"
                  :class="msg.type === 1 ? 'send' : 'replay'" />
              </div>
            </div>
          </div>
        </div>
      </main>

    </div>
    <footer id="footer">
      <div class="relative p-4 w-full overflow-hidden text-gray-600 focus-within:text-gray-400 flex items-center">
        <a-textarea v-model:value="state.message" :auto-size="{ minRows: 3, maxRows: 5 }" placeholder="请输入消息..."
          @pressEnter="sendMessage($event)"
          class="appearance-none pl-10 py-2 w-full bg-white border border-gray-300 rounded-full text-sm placeholder-gray-800 focus:outline-none focus:border-blue-500 focus:border-blue-500 focus:shadow-outline-blue" />
        <span class="absolute inset-y-0 right-0 bottom-6 pr-6 flex items-end">
          <a-button shape="round" type="primary" @click="sendMessage($event)">发送</a-button>
        </span>
      </div>

    </footer>
  </div>
</template>

<style scoped>
body,
html {
  margin: 0;
  padding: 0;
}

#layout {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  background-color: #f0f2f5;
}

#header {
  box-shadow: 2px 5px 5px 0px rgba(102, 102, 102, 0.5);
  flex-shrink: 0;
}

#layout-body {
  flex-grow: 2;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

#footer {
  border-top: 1px rgb(228, 228, 228) solid;
  width: 100%;
  /* height: 100px; */
  flex-shrink: 0;
}

#main {
  flex-grow: 2;
}

.replay {
  float: left;
  clear: both;
}

.send {
  float: right;
  clear: both;
}
</style>
