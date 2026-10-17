# Git Guide – CNPM

> Team: **Thịnh + Nguyên**  
> Branch chính: `main`  
> Branch cá nhân: `thinh`, `nguyen`

---

## 1. Quy tắc team

- ❌ Không code trực tiếp trên `main`.
- ✅ Mỗi người code trên branch của mình.
- ✅ Trước khi code: cập nhật `main`.
- ✅ Commit nhỏ, message rõ ràng.
- ✅ Push code thường xuyên.
- ✅ Merge vào `main` thông qua Pull Request.
- ❌ Không `push --force` vào `main`.
- ❌ Không commit password, API key, `.env`.

---

## 2. Lần đầu setup

### Nếu project đã có `.git` nhưng chưa có remote

```bash
git remote add origin https://github.com/nhatnguyen10a1thd-png/CNPM.git
git remote -v
git fetch origin
git branch -a
```

Nếu GitHub đã có commit, **kiểm tra trước khi push**.

Nếu remote đã có project và muốn tải về máy mới:

```bash
git clone https://github.com/nhatnguyen10a1thd-png/CNPM.git
cd CNPM
```

---

## 3. Tạo branch cá nhân

### Thịnh

```bash
git switch -c thinh
git push -u origin thinh
```

### Nguyên

```bash
git switch -c nguyen
git push -u origin nguyen
```

---

## 4. Mỗi lần bắt đầu code

```bash
git switch main
git pull origin main

git switch thinh        # hoặc nguyen
git merge main
```

Sau đó mới bắt đầu code.

---

## 5. Khi hoàn thành code

Kiểm tra:

```bash
git status
```

Add:

```bash
git add .
```

Commit:

```bash
git commit -m "feat: add author service"
```

Push:

```bash
git push
```

Sau đó tạo **Pull Request → `main`**.

---

## 6. Commit convention

```text
feat:     thêm chức năng
fix:      sửa lỗi
refactor: thay đổi cấu trúc code
test:     thêm/sửa test
docs:     tài liệu
chore:    cấu hình/dependency
```

Ví dụ:

```bash
git commit -m "feat: add author repository"
git commit -m "fix: fix author controller mapping"
git commit -m "test: add author service tests"
```

---

## 7. Sau khi `main` có code mới

```bash
git switch main
git pull origin main

git switch thinh        # hoặc nguyen
git merge main
```

Nếu có conflict:

```bash
# sửa file conflict

git add .
git commit -m "fix: resolve merge conflict"
git push
```

---

## 8. Lệnh hay dùng

```bash
git status                         # trạng thái
git branch                        # branch hiện tại
git branch -a                     # tất cả branch
git switch main                   # chuyển branch
git switch -c ten-branch          # tạo branch
git add .                         # stage tất cả
git commit -m "message"           # commit
git push                          # push
git pull                          # pull
git fetch                         # lấy thông tin remote
git merge main                    # merge main vào branch hiện tại
git remote -v                     # xem remote
git log --oneline --graph --all   # xem lịch sử
git diff                          # xem thay đổi
```

---

## 9. Undo cơ bản

Bỏ `git add`:

```bash
git restore --staged .
```

Bỏ thay đổi chưa commit của file:

```bash
git restore <file>
```

> ⚠️ `git restore` có thể làm mất thay đổi chưa commit.

Không dùng nếu chưa hiểu rõ:

```bash
git reset --hard
git push --force
```

---

## 10. Khi Git có vấn đề

Chạy 4 lệnh này trước:

```bash
git status
git branch -a
git remote -v
git log --oneline --graph --all
```

Nếu vẫn không biết xử lý → **đừng chạy lệnh reset/force**, hỏi teammate hoặc kiểm tra Git Guide.

---

## 11. Workflow nhớ nhanh

```text
START
  ↓
git switch main
git pull origin main
  ↓
git switch thinh/nguyen
git merge main
  ↓
CODE
  ↓
git status
git add .
git commit -m "feat: ..."
git push
  ↓
PULL REQUEST
  ↓
REVIEW
  ↓
MERGE → main
```
