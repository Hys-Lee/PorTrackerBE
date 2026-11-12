---
name: speckit-specify
description: 명세 — 기능 폴더에 spec.md 를 쓰고, 모르는 것을 모름 절에 드러낸다.
argument-hint: "Describe the feature you want to specify"
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: github-spec-kit
  source: preset:agentic-sdd
user-invocable: true
disable-model-invocation: false
---

# Speckit Specify Skill

## 받은 글

```text
$ARGUMENTS
```

## 할 일

1. **기능 폴더를 정한다.**
   - 받은 글의 첫 줄이 `폴더: <자리>` 면 그 자리를 쓴다.
   - 아니면 사람에게 묻고, 답을 받기 전에는 나아가지 않는다. 자리는 `specs/<도메인>/<기능>` 이고, 도메인이 없는 기능은 `specs/<기능>` 이다. 도메인 목록은 `.agentic/project.json` 의 `도메인들` 에 있다 — 목록을 보여 주고 고르게 한다.
   - 폴더를 만들고 `.specify/feature.json` 에 `{ "feature_directory": "<자리>" }` 를 쓴다. 뒤 단계가 이 파일로 폴더를 찾는다.

2. **`<자리>/spec.md` 를 쓴다.** 절은 이 차례다.
   - `## 목적` — 누구의 무슨 문제를 푸는가.
   - `## 사용 장면` — 사람이 무엇을 하고 무엇을 보는가.
   - `## 요구` — 하나마다 시험할 수 있게 적는다. 「빠르게」가 아니라 「1초 안에」.
   - `## 성공 기준` — 끝났다고 말할 수 있는 잴 수 있는 조건.
   - `## 모름` — 받은 글이 정하지 않았고 네가 정하면 안 되는 것. 한 줄에 하나씩 `- [모름: <물음> · <누가 답하나>]` 꼴로 적는다. 절 머리 바로 밑에 답하는 법을 한 줄 적는다: 「줄 끝에 `→ 답: <답>` 을 붙인다. 설계에 맡기려면 `→ 답: 설계에서 AI 가 정한다`.」

3. 🔴 **기본값을 지어내지 않는다.** 받은 글에 없는 값(한도·권한·보관 기간·오류 때 할 일 같은 것)은 그럴듯한 값으로 채우지 말고 모름 절에 적는다. 지어낸 값은 요구처럼 읽혀 아무도 다시 묻지 않는다. 받은 글에서 바로 따라 나오는 것만 요구에 적는다.

4. 기술 선택(언어·틀·저장소)은 적지 않는다. 그것은 설계 단계와 `directives/STRATEGY.md` 몫이다.

5. 끝에 사람에게 알린다: 쓴 자리, 모름 항목의 개수, 그리고 「모름을 채운 뒤 설계로 넘어간다」.
