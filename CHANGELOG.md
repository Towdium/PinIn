# Changelog

### 1.2.0

Changed match rules:

> Tone is no longer acceptable when missing vowel, e.g. "测试文本" no longer matches "c4shi", but still matches "ce4shi". Pinyin sequence (音序) can be used as abbreviation in Quanpin (全拼), e.g. "测试文本" matches "cswb" now without any fuzzy option, while in previous versions it only matches "cshwb".

Added switch for accelerator, disabled by default:

> When calling immediate matching functions continuously with different `s1` but same `s2`, for, say, 100 times, they are considered stable calls. If the scenario uses mainly stable calls and most of `s1` contains Chinese characters, using accelerator provides significant speed up. Otherwise, the overhead will slow down the matching process. Accelerate mode is disabled by default for consistency in different scenarios.

Fixed several spelling issues in Daqian (大千) keyboard  
Changed some APIs

### 1.3.0

Added support for customized dictionary loading  
Improved `CachedSearcher` math model for massive data  
Fixed incorrect spelling in Phonetic keyboard starting with `v`

### 1.3.1

Fixed `DictLoader` accessibility issue