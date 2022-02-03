# 🕐5분크롤러 - 코딩 없이 커스터마이징 가능한 크롤러
_가능한 한 빠르게 (5분 내에) 커스텀 가능하도록 설계되었습니다. (사전 지식을 갖추신 분에 한해서, 아마도요....)_

**⚠이 프로그램은 매우 실험적인 상태이며 예기치 못한 동작이 일어날 수 있습니다.**

[English Description](README.md)

## 이게 뭔가요?

**5분크롤러** 는 기본적으로 간단한 크롤러지만, JSON 파일을 통해 고도로 커스터마이징이 가능하도록 설계되었습니다.

이러한 기능을 통해 특정 사이트를 크롤링하는 프로그램을 매우 빠르게 구축할 수 있습니다.

## 기능

- 사용자가 지정한 조건 (주로 정규식)에 따라 재귀적으로 웹 페이지를 다운로드합니다.
- 강력한 파싱 라이브러리인 [jsoup](https://github.com/jhy/jsoup) 을 통해서 HTML을 파싱합니다.
- 사용자가 원하는 파일을 원하는 방식 (폴더, 사진이나 파싱된 웹 페이지)를 저장합니다.
- 이전에 페이지를 다운로드 받았을 경우 다시 다운로드하지 않습니다(사용자가 원하는 경우에만)

## 시작하기

먼저 질문, 개선사항과 같이 하고 싶은 말이 있다면, 이슈 탭을 이용해주세요.

### 명령 줄 옵션

    -u                         크롤링이 시작될 URL을 입력합니다.
    -p                         사용자가 커스터마이징한 JSON 파일의 경로를 입력합니다.
    -r                         (옵션) 이미 한 번 다운로드한 웹 페이지를 요청하고 싶지 않을 경우,
                               재개 파일의 경로를 입력합니다. (이 프로그램에 의해 생성됨)
    -o                         (옵션) 파일을 저장할 루트 디렉토리를 지정합니다.
                               기본값은 현재 프로그램이 저장된 장소입니다.
    -v                         장황한 로그를 사용합니다 (디버그용).

### 질문과 대답

- Q: 일부 요청이 수행되지 않습니다.
- A: 같은 URL을 가지고 있거나, 같은 alias(별칭)을 가진 [tag](GUIDE.md#Tag) 를 가지고 있다면 요청을 수행하지 않습니다.


- Q: 일부 내용이 저장되지 않습니다.
- A: 현재 파싱은 자바스크립트를 제외하고 실행됩니다. 원하는 내용이 자바스크립트를 꺼도 정상적으로 표시되는지 확인해주세요.


### 어떻게 커스터마이징 하나요?
[커스터마이징 가이드](/GUIDE_KO.md)를 보세요.

### 추가 설정

```json
{
  "MaxRequestThread": 1,
  "MaxPageLimit" : 100
}
```

| 설정 이름            |             기능              |
|:-----------------|:---------------------------:|
| MaxRequestThread | 얼마나 많은 웹 요청을 동시에 수행할지 결정합니다 |
| MaxPageLimit     |   수행될 웹 요청의 최대 개수를 제한합니다    |

현재는 이것 뿐입니다.... fivemin.config.json 에서 수정할 수 있습니다.

## 제한

- 현재 자바스크립트를 지원하지는 않지만, Selenium을 통한 지원이 예정되어 있습니다.

## 사용한 라이브러리

> [arrow](https://github.com/arrow-kt/arrow)
>
> [brotli](https://github.com/google/brotli)
>
> [jsoup](https://github.com/jhy/jsoup)
>
> [kotlin-logging](https://github.com/MicroUtils/kotlin-logging)
>
> [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)
>
> [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
>
> [kotlinx-cli](https://github.com/Kotlin/kotlinx-cli)
>
> [mockk](https://github.com/mockk/mockk)
>
> [okhttp](https://github.com/square/okhttp)
>
> [slf4j](https://github.com/qos-ch/slf4j)
>