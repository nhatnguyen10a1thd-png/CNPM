# Git Tutorial – Hướng dẫn sử dụng Git & GitHub cho Project LUNEA

Tài liệu này hướng dẫn các lệnh Git cơ bản và quy tắc làm việc chung của nhóm khi cộng tác trên project. Áp dụng cho cả thành viên mới bắt đầu dùng Git.

---

## 1. Cấu hình Git lần đầu

```bash
git --version                                  # kiểm tra đã cài Git chưa
git config --global user.name "Họ Tên"
git config --global user.email "email@example.com"
git config --global init.defaultBranch main
```

## 2. Ba khu vực làm việc của Git

```
Working Directory  --git add-->  Staging Area  --git commit-->  Repository (local)  --git push-->  Remote (GitHub)
```

| Khu vực | Ý nghĩa |
|---|---|
| Working Directory | Nơi bạn đang chỉnh sửa file |
| Staging Area | File đã `add`, chuẩn bị commit |
| Local Repository | Lịch sử commit trên máy bạn |
| Remote (GitHub) | Kho chứa dùng chung của cả nhóm |

## 3. Bắt đầu với project

```bash
git clone <url-repo>          # tải project về máy (chỉ làm 1 lần)
cd <ten-project>
git remote -v                 # kiểm tra remote đang trỏ đúng repo
git status                    # xem trạng thái hiện tại
```

---

## 4. Quy trình làm việc hằng ngày

**Bước 1 – Cập nhật code mới nhất trước khi làm việc**
```bash
git checkout main
git pull origin main
```

**Bước 2 – Tạo nhánh riêng cho công việc của bạn**
```bash
git checkout -b feature/gio-hang
```

**Bước 3 – Code, sau đó thêm & commit**
```bash
git status                    # xem file nào đã thay đổi
git add .                     # đưa vào staging (hoặc git add <tên-file>)
git commit -m "feat: thêm chức năng giỏ hàng"
```

**Bước 4 – Đẩy nhánh lên GitHub**
```bash
git push origin feature/gio-hang
```

**Bước 5 – Tạo Pull Request (PR) trên GitHub**
→ Vào repo trên GitHub → *Compare & pull request* → mô tả thay đổi → yêu cầu thành viên còn lại review → **Merge** sau khi được duyệt.

> Lặp lại chu trình pull → branch → commit → push → PR cho mỗi tính năng/công việc mới.

---

## 5. Quy tắc đặt tên nhánh (branch)

| Loại nhánh | Dùng khi | Ví dụ |
|---|---|---|
| `main` | Code ổn định, luôn chạy được, không code trực tiếp trên đây | — |
| `feature/<tên>` | Phát triển tính năng mới | `feature/dang-nhap` |
| `fix/<tên>` | Sửa lỗi thông thường | `fix/loi-tinh-tong-gio-hang` |
| `hotfix/<tên>` | Sửa lỗi khẩn cấp | `hotfix/loi-thanh-toan` |

Quy tắc: chữ thường, không dấu, cách nhau bằng dấu `-`.

## 6. Quy tắc viết commit message

Format: `<loại>: <mô tả ngắn gọn, rõ ràng>`

| Loại | Ý nghĩa |
|---|---|
| `feat` | Thêm tính năng mới |
| `fix` | Sửa lỗi |
| `docs` | Thay đổi tài liệu (README, tutorial...) |
| `style` | Chỉnh format, không đổi logic |
| `refactor` | Tái cấu trúc code, không đổi chức năng |
| `test` | Thêm/sửa test |
| `chore` | Việc linh tinh (cấu hình, cập nhật thư viện...) |

**Ví dụ:** `fix: sửa lỗi không giữ tồn kho khi đặt hàng`

Nên commit **nhỏ và thường xuyên**, mỗi commit làm một việc rõ ràng — tránh commit kiểu `"update"`, `"fix bug"` chung chung.

---

## 7. Branch & Merge

```bash
git branch                    # xem các nhánh local
git branch -a                 # xem cả nhánh remote
git checkout <tên-nhánh>      # chuyển nhánh (hoặc: git switch <tên-nhánh>)
git merge <tên-nhánh>         # gộp nhánh đó vào nhánh hiện tại
```

**Khuyến nghị:** luôn merge qua **Pull Request trên GitHub** thay vì `git merge` thủ công trên `main`, để có bước review và tránh đẩy nhầm code lỗi lên nhánh chính.

## 8. Xử lý Conflict (xung đột)

Conflict xảy ra khi 2 người cùng sửa **một dòng/khu vực** của cùng một file trên 2 nhánh khác nhau, và Git không tự biết chọn bản nào.

**Cách xử lý:**
```bash
git pull origin main          # hoặc git merge main khi đang ở nhánh của bạn
```
1. Git báo `CONFLICT` và đánh dấu trong file bị xung đột:
   ```
   <<<<<<< HEAD
   code của bạn
   =======
   code của người khác
   >>>>>>> feature/ten-nhanh
   ```
2. Mở file, **sửa lại nội dung đúng** (giữ 1 bản, kết hợp cả 2, hoặc viết lại), xóa các dòng `<<<<<<<`, `=======`, `>>>>>>>`.
3. Đánh dấu đã xử lý xong và commit:
   ```bash
   git add <file-vừa-sửa>
   git commit -m "fix: giải quyết conflict ở <file>"
   git push
   ```

Nếu muốn hủy merge để làm lại: `git merge --abort`.

> Mẹo tránh conflict: `pull` thường xuyên, chia nhỏ công việc theo file/module khác nhau, commit sớm và đừng để nhánh "sống" quá lâu trước khi merge.

---

## 9. Một số lệnh hữu ích khác

```bash
git log --oneline --graph --all   # xem lịch sử commit dạng cây
git diff                          # xem thay đổi chưa add
git stash                         # tạm cất thay đổi chưa commit
git stash pop                     # lấy lại thay đổi vừa cất
git revert <mã-commit>            # tạo commit mới để hủy 1 commit cũ (an toàn)
git reset --soft HEAD~1           # bỏ commit gần nhất, giữ lại thay đổi
```

⚠️ Tránh dùng `git reset --hard` và `git push --force` trên nhánh `main` hoặc nhánh dùng chung — có thể làm mất commit của người khác.

## 10. File `.gitignore` gợi ý

```
node_modules/
.env
*.log
.DS_Store
/dist
/build
```

---

## 11. Quy tắc chung của nhóm

1. **Không** commit/push trực tiếp lên `main` — luôn làm qua nhánh riêng + Pull Request.
2. **Luôn `pull`** trước khi bắt đầu code và trước khi `push`.
3. Mỗi nhánh chỉ làm **một việc**; đặt tên và commit message theo đúng quy tắc ở mục 5–6.
4. Review code của nhau trước khi **Merge** Pull Request.
5. Sau khi merge xong, xóa nhánh đã dùng để repo gọn gàng:
   ```bash
   git branch -d feature/gio-hang
   git push origin --delete feature/gio-hang
   ```
6. Nếu không chắc chắn về lệnh Git nào (đặc biệt `reset --hard`, `push --force`), hỏi trước khi chạy.
