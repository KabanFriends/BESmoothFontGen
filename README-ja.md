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
            "type": "file",
            "file": "noto-jp-regular.ttf",
            "size": 38,
            "padding": 0,
            "additionalArgs": "-pxrange 8 -translate 0 6"
        }
    ]
}
```

### 設定項目
- `threads`: フォントのテクスチャ生成に使用するスレッドの数です。
- `range`: 生成するUnicode文字の範囲です。Unicodeの0x0000から0x02FFまでの範囲(画像3枚)のフォントを生成したい場合、`from`の項目に`00`、`to`の項目に`02`を指定してください。
- `fonts`: 生成時に使用するフォント設定の配列です。各文字を生成するときは、配列の先頭から順にフォントが読み込まれ、生成対象の文字がそのフォントに存在しない場合は次のフォントが読み込まれます。
    - `file`: フォントのファイル名です。ここに指定したファイルを`fonts`フォルダ内に配置する必要があります。
    - `size`: 文字サイズをポイント単位で指定します。
    - `padding`: 文字幅に追加する空白の量です。
    - `additionalArgs`: フォントテクスチャの生成時、msdfgenに渡す追加のパラメータです。各パラメータの詳細情報は[Chlumsky/msdfgen](https://github.com/Chlumsky/msdfgen)を確認してください。
- `cellMapping`（任意）: `remapping.dat`を使って、文字を別の文字のテクスチャセルに割り当てます。配列を直接書くか、`mappings`プロパティに配列を持つJSONファイルのパスを指定します。
    ```json
    {
        "mappings": [
            { "char": "0410", "cell": "0041" },
            { "char": "2660", "cell": "0410" }
        ]
    }
    ```
    各エントリーにより、`char`は`cell`のセルにあるテクスチャ**と文字幅**で表示されます。コードポイントは16進数で指定します。
    - 文字を自分のセルから別のセルに割り当てると、元のセルは空きます。そのセルに割り当てられた文字のうち最も小さいものがそこに描画されます（上の例では、U+0410のセルにU+2660 ♠が描画されます）。何も割り当てられていなければ、そのセルは空になります。
    - それ以外にセルへ割り当てられた文字は、セルの持ち主と完全に同じ見た目（トレースされたグリフ、描画パラメータ、文字幅）でなければなりません。生成前にこれを確認し、一致しない組み合わせがあれば生成を中止します。
    - `cell`はクライアントが読み込むページ上にある必要があり、同じ文字を複数回割り当てることはできません。
- `pageRemapping`（任意・旧方式）: ページ全体を別のページのセルに描画します。各エントリーは`cellMapping`と同じ文字単位の割り当てに展開されます。割り当て先ページのセルを`cellMapping`で使用することはできません。
- `remapHangulChars`（任意・旧方式）: `pageRemapping`使用時、割り当て先がハングル音節の範囲のページであっても、そのページ本来の文字をU+FFFEに転送します。それ以外の割り当て先ページは常に転送されます。

## ビルド
以下のコマンドを使用すると、BESmoothFontGenを自分でビルドできます。
```shell
./gradlew build
```
ビルドが完了すると、`build/libs`フォルダにjarファイルが生成されます。
