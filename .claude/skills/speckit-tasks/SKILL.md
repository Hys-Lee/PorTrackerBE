---
name: speckit-tasks
description: 작업 목록 — 설계의 마일스톤마다 할 일을 task.md 에 쓴다. 사람의 지시가 있으면 따르고, 없으면 알아서 정한다.
argument-hint: "Optional task generation constraints"
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:agentic-sdd
user-invocable: true
disable-model-invocation: false
---

# Speckit Tasks Skill

## 받은 글

```text
$ARGUMENTS
```

## 할 일

1. **기능 폴더를 찾는다.** 받은 글의 첫 줄이 `폴더: <자리>` 면 그 자리이고, 아니면 `.specify/feature.json` 의 `feature_directory` 다. 둘 다 없으면 사람에게 묻는다.

2. **읽는다.** `<자리>/spec.md`, `<자리>/design.md`, `directives/CONVENTION.md`.

3. **사람의 지시를 모은다.** 지시는 두 곳에서 온다.
   - 받은 글에서 폴더 줄을 뺀 나머지
   - `<자리>/task-guide.md` (사람이 쓴 파일, 없을 수 있다)

   지시가 있으면 그대로 따른다 — 쪼개는 단위, 차례, 이름 붙이는 법, 빼라는 것 모두. 🔴 두 지시가 서로 어긋나면 쓰지 말고 어긋난 곳을 보이고 멈춘다. 어느 쪽이 맞는지는 사람이 정한다.

   지시가 없으면 네가 정하고, `task.md` 맨 위에 어떻게 쪼갰는지 한 줄로 적는다.

4. **`<자리>/task.md` 를 쓴다.**
   - 설계의 마일스톤마다 `## <마일스톤>` 절을 둔다.
   - 절 안에 `- [ ] <할 일> — <파일 자리>` 줄을 둔다. 할 일은 클래스·컴포넌트·스키마·인터페이스 단위로 적고, 그것이 사는 파일 자리를 같이 적는다.
   - 시험을 먼저 쓸지 같은 방법은 `directives/CONVENTION.md` 가 정한다. 거기 없는 방법을 지어 넣지 않는다.

5. 끝에 사람에게 알린다: 쓴 자리, 마일스톤마다 할 일 개수, 지시를 따랐는지 알아서 정했는지.
