import type { RoleCode } from '@/constants/roles';
import { ROUTES } from '@/constants/routes';

/** Một nhóm chức năng trên trang chính; chỉ hiện với các vai trò được liệt kê (QTN-01). */
export interface FunctionGroup {
  key: string;
  title: string;
  description: string;
  roles: readonly RoleCode[];
  /** Đường dẫn khi chức năng đã có; chưa có thì hiển thị "Sắp ra mắt". */
  to?: string;
}

/** Nhóm chức năng theo quyền hạn chính của từng vai trò (mục 2 của backlog). */
export const FUNCTION_GROUPS: readonly FunctionGroup[] = [
  {
    key: 'checkin',
    title: 'Ra vào câu lạc bộ',
    description: 'Quét thẻ, mã động, khách đi kèm, tủ đồ',
    roles: ['RECEPTIONIST'],
  },
  {
    key: 'members',
    title: 'Hồ sơ hội viên',
    description: 'Lập hồ sơ, kích hoạt gói, cấp thẻ',
    roles: ['RECEPTIONIST'],
  },
  {
    key: 'cashier',
    title: 'Thu tiền và chốt ca',
    description: 'Phiếu thu, mã quét chuyển khoản, bàn giao ca',
    roles: ['RECEPTIONIST'],
  },
  {
    key: 'classBooking',
    title: 'Đặt chỗ lớp nhóm',
    description: 'Đặt và hủy chỗ lớp cho hội viên tại quầy',
    roles: ['RECEPTIONIST'],
  },
  {
    key: 'counterSales',
    title: 'Bán hàng tại quầy',
    description: 'Nước, đồ dùng tập, cho thuê khăn, vé ngày',
    roles: ['RECEPTIONIST'],
  },
  {
    key: 'clubOperations',
    title: 'Lịch lớp và ca làm',
    description: 'Xếp lịch lớp nhóm, ca làm, huấn luyện viên phụ trách',
    roles: ['CLUB_MANAGER'],
  },
  {
    key: 'approvals',
    title: 'Phê duyệt',
    description: 'Giảm giá trong hạn mức, hủy phiếu thu, chênh lệch ca',
    roles: ['CLUB_MANAGER'],
  },
  {
    key: 'safety',
    title: 'An toàn và biên bản',
    description: 'Biên bản tai nạn, giấy xác nhận sức khỏe',
    roles: ['CLUB_MANAGER'],
  },
  {
    key: 'counterDevice',
    title: 'Đăng ký máy quầy',
    description: 'Đăng ký máy đang dùng làm máy quầy lễ tân của câu lạc bộ',
    roles: ['CLUB_MANAGER', 'ADMIN'],
    to: ROUTES.counterRegister,
  },
  {
    key: 'catalog',
    title: 'Gói tập, giá và khuyến mại',
    description: 'Danh mục gói, bảng giá, chương trình khuyến mại',
    roles: ['CHAIN_OWNER'],
  },
  {
    key: 'contracts',
    title: 'Mẫu hợp đồng',
    description: 'Hợp đồng theo mẫu và điều kiện giao dịch chung',
    roles: ['CHAIN_OWNER'],
  },
  {
    key: 'refunds',
    title: 'Duyệt hoàn tiền',
    description: 'Hoàn tiền và giảm giá vượt hạn mức quản lý',
    roles: ['CHAIN_OWNER'],
  },
  {
    key: 'dashboard',
    title: 'Bảng điều khiển',
    description: 'Doanh thu ghi nhận, hội viên, gia hạn của toàn chuỗi',
    roles: ['CHAIN_OWNER', 'CLUB_MANAGER'],
  },
  {
    key: 'leads',
    title: 'Khách tiềm năng',
    description: 'Tiếp nhận, chăm sóc khách và buổi tập thử',
    roles: ['SALES_CONSULTANT'],
  },
  {
    key: 'sales',
    title: 'Hợp đồng gói tập',
    description: 'Tư vấn, lập hợp đồng, nâng cấp gói',
    roles: ['SALES_CONSULTANT'],
  },
  {
    key: 'commission',
    title: 'Hoa hồng của tôi',
    description: 'Hoa hồng tính trên tiền thực thu',
    roles: ['SALES_CONSULTANT'],
  },
  {
    key: 'ptSchedule',
    title: 'Lịch tập cá nhân',
    description: 'Xếp lịch và xác nhận buổi tập với hội viên',
    roles: ['PERSONAL_TRAINER'],
  },
  {
    key: 'progress',
    title: 'Chỉ số cơ thể và giáo án',
    description: 'Đo chỉ số, soạn giáo án cho hội viên phụ trách',
    roles: ['PERSONAL_TRAINER'],
  },
  {
    key: 'myClasses',
    title: 'Lớp của tôi',
    description: 'Danh sách đặt chỗ, điểm danh, báo bận',
    roles: ['GROUP_TRAINER'],
  },
  {
    key: 'trainerPay',
    title: 'Thù lao của tôi',
    description: 'Buổi đã dạy và thù lao theo kỳ',
    roles: ['PERSONAL_TRAINER', 'GROUP_TRAINER'],
  },
  {
    key: 'invoices',
    title: 'Thuế suất và hóa đơn',
    description: 'Thuế suất theo nhóm dịch vụ, hóa đơn điện tử',
    roles: ['ACCOUNTANT'],
  },
  {
    key: 'dayClose',
    title: 'Chốt ngày',
    description: 'Đối chiếu phiếu thu, hóa đơn và tiền về tài khoản',
    roles: ['ACCOUNTANT'],
  },
  {
    key: 'payouts',
    title: 'Hoàn tiền, hoa hồng, thù lao',
    description: 'Xử lý hoàn tiền, tính hoa hồng và thù lao',
    roles: ['ACCOUNTANT'],
  },
  {
    key: 'equipment',
    title: 'Sổ thiết bị',
    description: 'Kiểm tra an toàn, báo hỏng, bảo trì định kỳ',
    roles: ['TECHNICIAN'],
  },
  {
    key: 'retention',
    title: 'Bảo lưu, chuyển nhượng, gia hạn',
    description: 'Quyền lợi của hội viên trong thời gian dùng gói',
    roles: ['MEMBER_CARE'],
  },
  {
    key: 'care',
    title: 'Khiếu nại và chăm sóc',
    description: 'Khiếu nại, hội viên lâu không đến, tin quảng bá',
    roles: ['MEMBER_CARE'],
  },
  {
    key: 'accounts',
    title: 'Tài khoản và phân quyền',
    description: 'Tài khoản nhân viên, vai trò, phạm vi câu lạc bộ',
    roles: ['ADMIN'],
  },
  {
    key: 'clubs',
    title: 'Câu lạc bộ',
    description: 'Câu lạc bộ, khu tập, giờ hoạt động',
    roles: ['ADMIN'],
  },
  {
    key: 'auditLogs',
    title: 'Nhật ký thao tác',
    description: 'Tra cứu ai đã làm gì, vào lúc nào',
    roles: ['ADMIN'],
  },
  {
    key: 'backup',
    title: 'Sao lưu và phục hồi',
    description: 'Sao lưu hằng đêm, phục hồi dữ liệu',
    roles: ['ADMIN'],
  },
];

export function functionGroupsFor(roleCodes: readonly RoleCode[]): FunctionGroup[] {
  return FUNCTION_GROUPS.filter((group) => group.roles.some((role) => roleCodes.includes(role)));
}
