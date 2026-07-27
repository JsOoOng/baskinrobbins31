/*
 * 쉬운주석: 배송 취소와 재고 신청 반려가 같은 사유 목록을 사용하도록 한곳에 모아 둔다.
 */
export const restockRejectionReasons = [
  '본사 요청',
  '지점 요청',
  '배송업체 자체 재고 부족',
  '배송 기사 미배정',
  '배송 차량 고장',
  '기상 악화',
  '배송지 정보 오류',
  '상품 파손',
  '상품 분실',
  '배송 불가 지역',
  '시스템 오류',
  '기타(직접 입력)'
]

/*
 * 쉬운주석: 간단한 반려 화면에서는 번호 또는 직접 입력으로 같은 사유를 받는다.
 */
export const askRestockRejectionReason = () => {
  const answer = window.prompt(
    `반려 사유를 번호로 선택하거나 직접 입력해주세요.\n\n${
      restockRejectionReasons.map((reason, index) => `${index + 1}. ${reason}`).join('\n')
    }`
  )?.trim()

  if (!answer) return null
  const selected = restockRejectionReasons[Number(answer) - 1]
  if (selected === '기타(직접 입력)') {
    return window.prompt('기타 반려 사유를 입력해주세요.')?.trim() || null
  }
  return selected ?? answer
}
