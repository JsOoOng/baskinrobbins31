<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  databaseName: { type: String, default: '' },
  targetLabel: { type: String, default: '데이터' },
  deleting: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'confirm'])
const typedText = ref('')
const expectedText = computed(() => `${props.databaseName}/삭제한다`)
const canDelete = computed(() => typedText.value.trim() === expectedText.value)

watch(() => props.open, (open) => {
  // 쉬운주석: 새 삭제 창을 열 때 이전에 입력한 확인 문구를 깨끗하게 지운다.
  if (open) typedText.value = ''
})

const close = () => {
  if (!props.deleting) emit('close')
}

const submit = () => {
  if (canDelete.value && !props.deleting) {
    emit('confirm', typedText.value.trim())
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="hard-delete-backdrop" @click.self="close">
      <form class="hard-delete-modal" @submit.prevent="submit">
        <div class="danger-icon">!</div>
        <h2>정말 지우시겠습니까?</h2>
        <p>
          선택한 {{ targetLabel }}와 연결된 데이터가 DB에서 영구 삭제됩니다.<br>
          이 작업은 되돌릴 수 없습니다.
        </p>

        <label>
          <span>지우시려면 해당 DB 이름을 정확히 입력하세요.</span>
          <strong>{{ expectedText }}</strong>
          <input
            v-model="typedText"
            type="text"
            autocomplete="off"
            :placeholder="expectedText"
            :disabled="deleting"
            autofocus
          >
        </label>

        <div class="hard-delete-actions">
          <button type="button" class="cancel" :disabled="deleting" @click="close">취소</button>
          <button type="submit" class="remove" :disabled="!canDelete || deleting">
            {{ deleting ? 'DB에서 삭제 중...' : '영구 삭제' }}
          </button>
        </div>
      </form>
    </div>
  </Teleport>
</template>

<style scoped>
.hard-delete-backdrop{position:fixed;inset:0;z-index:3000;display:grid;place-items:center;padding:20px;background:rgba(20,20,35,.58);backdrop-filter:blur(3px)}
.hard-delete-modal{width:min(440px,100%);padding:28px;border-radius:18px;background:#fff;box-shadow:0 24px 70px rgba(0,0,0,.25);text-align:center}
.danger-icon{display:grid;width:50px;height:50px;margin:0 auto 14px;place-items:center;border-radius:50%;background:#fff1f2;color:#dc2626;font-size:28px;font-weight:900}
h2{margin:0;color:#202333;font-size:21px}p{margin:12px 0 22px;color:#6b7280;font-size:13px;line-height:1.7}
label{display:grid;gap:8px;text-align:left}label span{color:#374151;font-size:12px;font-weight:800}label strong{padding:9px 11px;border-radius:8px;background:#fff1f2;color:#be123c;font:700 13px Consolas,monospace}
input{height:42px;padding:0 12px;border:1px solid #d7dce5;border-radius:10px;outline:none;font-size:13px}input:focus{border-color:#e25568;box-shadow:0 0 0 3px rgba(226,85,104,.12)}
.hard-delete-actions{display:flex;gap:9px;justify-content:flex-end;margin-top:22px}.hard-delete-actions button{height:40px;padding:0 17px;border-radius:10px;cursor:pointer;font-size:12px;font-weight:800}.cancel{border:1px solid #dfe3ea;background:#fff;color:#596273}.remove{border:0;background:#dc274d;color:#fff}.remove:disabled{cursor:not-allowed;background:#e5a8b4}.cancel:disabled{cursor:not-allowed;opacity:.55}
</style>
