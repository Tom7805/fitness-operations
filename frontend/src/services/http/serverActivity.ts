/**
 * Mốc gần nhất máy chủ ghi nhận một thao tác của phiên (mỗi yêu cầu có mã truy cập thành công).
 * Máy chủ tính hết phiên từ mốc này nên giao diện cũng đếm từ đây.
 */
let lastServerActivityAt = Date.now();

export const serverActivity = {
  mark(at: number = Date.now()): void {
    lastServerActivityAt = at;
  },
  lastAt(): number {
    return lastServerActivityAt;
  },
};
