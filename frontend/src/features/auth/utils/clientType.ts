/** Phân biệt điện thoại với máy tính để ghi đúng loại thiết bị vào phiên và nhật ký. */
export function detectClientType(): 'OFFICE' | 'MOBILE' {
  const navigatorWithHints = navigator as Navigator & { userAgentData?: { mobile?: boolean } };
  if (navigatorWithHints.userAgentData?.mobile === true) {
    return 'MOBILE';
  }
  return /Android|iPhone|iPod|Mobile|Windows Phone/i.test(navigator.userAgent)
    ? 'MOBILE'
    : 'OFFICE';
}
