**[English](README.md)** | **日本語**

# BESmoothFontGen
Minecraft: Bedrock Editionで使用される「smooth」形式のフォントのテクスチャやデータファイルを生成するツールです。
smoothフォントは、ゲーム内の以下に例を示す箇所で使用されているフォントの形式です。
- 言語設定を「日本語(ja_JP)」に設定した際のデフォルトフォント
- サーバー一覧画面
- 一部の説明文など

## 使用方法
### Windows
1. [Java 8](https://www.java.com/download/)をインストールする
2.  [BESmoothFontGen](https://github.com/KabanFriends/BESmoothFontGen/releases/latest)をダウンロードし、ZIPファイルを展開する
3. [msdfgen](https://github.com/Chlumsky/msdfgen/releases/latest)をダウンロードし、ZIPファイルを展開する
4. `msdfgen.exe`を、手順2で展開したBESmoothFontGenフォルダ内の`msdfgen`フォルダの中に配置する
5. `config.json`をテキストエディタで開き、 [設定](#設定)に示す項目を含むようにファイルを編集する
6. 設定で指定したフォントファイルを、`fonts`フォルダの中に配置する
7. `BESmoothFontGen.bat`を起動し、フォント生成を開始する
8. 生成が終わると、フォントのテクスチャとデータが`smooth`フォルダ内に配置されます

### Linux
インストールや使用手順は基本的にWindowsと同じですが、msdfgenの実行ファイルを自分でビルドする必要があります。詳細は[Chlumsky/msdfgen](https://github.com/Chlumsky/msdfgen)を確認してください。  
Linux上でのフォント生成は、Windowsに比べて数倍早くなります。

## 設定
### サンプル
```json
{
    "threads": 16,
    "range": {
        "from": "00",
        "to": "FF"
    },
    "fonts": [
        {
            "file": "noto-jp-regular.ttf",
            "size": 38,
            "padding": 0
        }
    ],
    "additionalArgs": "-pxrange 8 -translate 0 6"
}
```

### 設定項目
- `threads`: フォントのテクスチャ生成に使用する総スレッド数です。
- `range`: 生成するUnicode文字の範囲です。Unicodeの0x0000から0x02FFまでの範囲(画像3枚)のフォントを生成したい場合、`from`の項目に`00`、`to`の項目に`02`を指定してください。
- `fonts`: 生成時に使用するフォント設定の配列です。各文字を生成するときは、配列の先頭から順にフォントが読み込まれ、生成対象の文字がそのフォントに存在しない場合は次のフォントが読み込まれます。
    - `file`: フォントのファイル名です。ここに指定したファイルを`fonts`フォルダ内に配置する必要があります。
    - `size`: 文字サイズをポイント単位で指定します。
    - `padding`: 文字幅に追加する空白の量です。
- `additionalArgs`: フォントテクスチャの生成時、msdfgenに渡す追加のパラメータです。各パラメータの詳細情報は[Chlumsky/msdfgen](https://github.com/Chlumsky/msdfgen)を確認してください。

## ビルド
以下のコマンドを使用すると、BESmoothFontGenを自分でビルドできます。
```shell
./gradlew build
```
ビルドが完了すると、`build/libs`フォルダにjarファイルが生成されます。
