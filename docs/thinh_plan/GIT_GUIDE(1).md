# Git Tutorial – Team Project

> Cheat sheet để làm Git nhóm: branch → commit → push → pull → merge → conflict.

---

## 1. Workflow cơ bản

Mỗi lần bắt đầu task:

```bash
git switch main
git pull origin main
git switch -c feature/ten-feature
```

Sau khi code:

```bash
git status
git add .
git commit -m "feat: mo ta thay doi"
git push -u origin feature/ten-feature
```

Sau đó tạo **Pull Request → Review → Merge vào `main`**.

---

## 2. Kiểm tra Git

```bash
git status
git branch
git branch -a
git log --oneline --graph --all
git remote -v
```

---

## 3. Clone project

```bash
git clone <URL>
cd <project-folder>
```

---

## 4. Branch

### Xem branch

```bash
git branch
git branch -a
```

### Tạo branch

```bash
git switch -c feature/book-api
```

### Chuyển branch

```bash
git switch main
git switch feature/book-api
```

### Xóa branch local

```bash
git branch -d feature/book-api
```

Nếu branch chưa merge nhưng chắc chắn muốn xóa:

```bash
git branch -D feature/book-api
```

---

## 5. Commit

### Kiểm tra thay đổi

```bash
git status
git diff
```

### Add

Tất cả file:

```bash
git add .
```

Một file:

```bash
git add <file>
```

### Commit

```bash
git commit -m "feat: add book controller"
```

### Commit convention

```text
feat:     thêm chức năng
fix:      sửa lỗi
refactor: thay đổi code, không đổi chức năng
test:     thêm/sửa test
docs:     tài liệu
chore:    cấu hình/project
```

Ví dụ:

```bash
git commit -m "feat: add author service"
git commit -m "fix: fix author update"
git commit -m "test: add author controller tests"
```

> Nên commit nhỏ, mỗi commit nên tập trung vào một thay đổi.

---

## 6. Push

Push branch lần đầu:

```bash
git push -u origin feature/book-api
```

Những lần sau:

```bash
git push
```

> Khi làm nhóm, hạn chế push trực tiếp vào `main`. Nên dùng Pull Request.

---

## 7. Pull

Cập nhật branch hiện tại:

```bash
git pull
```

Hoặc:

```bash
git pull origin main
```

Trước khi bắt đầu task:

```bash
git switch main
git pull origin main
```

---

## 8. Fetch

Lấy thông tin mới từ GitHub nhưng chưa merge:

```bash
git fetch origin
```

Xem branch remote:

```bash
git branch -a
```

Tạo local branch từ remote:

```bash
git switch -c feature/book-api origin/feature/book-api
```

---

## 9. Merge

Merge `feature/book-api` vào `main`:

```bash
git switch main
git pull origin main
git merge feature/book-api
```

Nếu không có conflict:

```bash
git push origin main
```

### Workflow khuyến nghị

```text
feature branch
      ↓
    push
      ↓
Pull Request
      ↓
   Review
      ↓
   Merge
      ↓
    main
```

---

## 10. Cập nhật branch bằng main

Trong lúc bạn code, teammate có thể đã merge code mới vào `main`.

```bash
git switch main
git pull origin main

git switch feature/book-api
git merge main
```

Sau đó test lại.

---

## 11. Conflict

Conflict xảy ra khi Git không thể tự quyết định nên giữ thay đổi nào.

Ví dụ:

```text
<<<<<<< HEAD
return "Book";
=======
return "Books";
>>>>>>> main
```

Ý nghĩa:

```text
<<<<<<< HEAD
Code hiện tại của bạn
=======
Code từ branch được merge
>>>>>>> main
```

### Xử lý

Mở file → chọn/sửa code đúng → xóa các marker:

```text
<<<<<<<
=======
>>>>>>>
```

Sau đó:

```bash
git status
git add .
git commit -m "fix: resolve merge conflict"
```

Nếu đang merge và muốn hủy:

```bash
git merge --abort
```

---

## 12. Pull bị conflict

```bash
git pull origin main
```

Nếu conflict:

```bash
git status
```

Sửa các file conflict → sau đó:

```bash
git add .
git commit -m "fix: resolve merge conflict"
git push
```

---

## 13. Undo

### Bỏ thay đổi chưa add

```bash
git restore <file>
```

Bỏ tất cả:

```bash
git restore .
```

> Cẩn thận: thay đổi chưa commit sẽ mất.

### Bỏ file khỏi staging

```bash
git restore --staged <file>
```

### Sửa commit cuối

```bash
git commit --amend
```

> Chỉ nên dùng khi commit chưa được push/shared.

---

## 14. Sau khi merge

```bash
git switch main
git pull origin main
```

Xóa branch local:

```bash
git branch -d feature/book-api
```

Xóa branch trên GitHub:

```bash
git push origin --delete feature/book-api
```

Sau đó tạo task mới:

```bash
git switch -c feature/new-task
```

---

## 15. Quy trình làm việc nhóm

### Người nhận task

```bash
git switch main
git pull origin main
git switch -c feature/ten-task
```

### Code xong

```bash
git status
git add .
git commit -m "feat: mo ta task"
git push -u origin feature/ten-task
```

### Tạo Pull Request

```text
feature/ten-task
       ↓
     Push
       ↓
Pull Request
       ↓
   Teammate Review
       ↓
     Merge
       ↓
      main
```

### Trước khi merge

```bash
git switch main
git pull origin main

git switch feature/ten-task
git merge main
```

Nếu conflict → sửa → test → push:

```bash
git add .
git commit -m "fix: resolve merge conflict"
git push
```

---

## 16. Quy tắc nhóm

### Không code trực tiếp trên main

```text
main
 ↑
Pull Request
 ↑
feature/...
```

### Luôn pull trước khi bắt đầu task

```bash
git switch main
git pull
```

### Commit thường xuyên

Không nên:

```text
1 commit → 500 dòng → nhiều chức năng
```

Nên:

```text
feat: add entity
feat: add repository
feat: add service
feat: add controller
test: add controller tests
```

### Commit message rõ ràng

Không nên:

```bash
git commit -m "update"
git commit -m "fix"
git commit -m "abc"
```

Nên:

```bash
git commit -m "feat: add author controller"
git commit -m "fix: fix author update mapping"
```

### Trước khi push

```bash
git status
git diff
```

Kiểm tra không commit nhầm:

```text
.env
target/
.idea/
*.class
```

### Không dùng bừa

```bash
git reset --hard
git push --force
```

Nếu không chắc:

```bash
git status
git log --oneline --graph --all
```

---

## 17. Cheat Sheet

```bash
# Kiểm tra
git status
git branch
git log --oneline --graph --all

# Cập nhật main
git switch main
git pull origin main

# Tạo branch
git switch -c feature/ten-feature

# Commit
git status
git add .
git commit -m "feat: description"

# Push
git push -u origin feature/ten-feature

# Cập nhật branch bằng main
git switch main
git pull origin main
git switch feature/ten-feature
git merge main

# Conflict
git status
# sửa file
git add .
git commit -m "fix: resolve merge conflict"

# Hủy merge
git merge --abort

# Sau khi merge
git switch main
git pull
git branch -d feature/ten-feature
```

---

## 18. Workflow cần nhớ

```text
START
  │
  ▼
git switch main
  │
  ▼
git pull
  │
  ▼
git switch -c feature/...
  │
  ▼
    CODE
  │
  ▼
git add .
  │
  ▼
git commit
  │
  ▼
git push
  │
  ▼
Pull Request
  │
  ▼
Review
  │
  ├── Conflict? → Fix → Push
  │
  ▼
Merge → main
  │
  ▼
git pull
  │
  ▼
NEXT TASK
```

> **5 lệnh quan trọng nhất:**
>
> `git switch` → chuyển branch  
> `git pull` → lấy code mới  
> `git add` → chuẩn bị thay đổi  
> `git commit` → lưu thay đổi  
> `git push` → đưa code lên GitHub
