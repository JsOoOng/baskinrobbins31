import { reactive } from 'vue'

// 쉬운주석: 어느 본사 화면에서든 같은 삭제 확인 모달을 열 수 있게 상태를 한곳에 둔다.
export const hardDeleteDialogState = reactive({
  open: false,
  databaseName: '',
  targetLabel: '',
  deleting: false,
})

let finishRequest = null

export const requestHardDeleteConfirmation = ({ databaseName, targetLabel }) =>
  new Promise((resolve) => {
    // 쉬운주석: 이전 창이 남아 있다면 취소로 끝낸 뒤 새 대상의 창을 연다.
    if (finishRequest) finishRequest(null)
    finishRequest = resolve
    hardDeleteDialogState.databaseName = String(databaseName ?? '').trim()
    hardDeleteDialogState.targetLabel = targetLabel || '데이터'
    hardDeleteDialogState.open = true
  })

export const closeHardDeleteDialog = () => {
  hardDeleteDialogState.open = false
  const resolve = finishRequest
  finishRequest = null
  if (resolve) resolve(null)
}

export const confirmHardDeleteDialog = (confirmation) => {
  hardDeleteDialogState.open = false
  const resolve = finishRequest
  finishRequest = null
  if (resolve) resolve(confirmation)
}
