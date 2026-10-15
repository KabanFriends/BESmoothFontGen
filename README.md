**English** | **[日本語](README-ja.md)**

# BESmoothFontGen
A tool that generates "smooth" font textures and data for Minecraft: Bedrock Edition.
The smooth font is a special variant of the in-game font seen in certain places within the game such as:
- Default font for the Japanese language
- Server list screen
- Various miscellaneous descriptions

## How to Use
### Windows
1. Install [Java 8](https://www.java.com/download/)
2. Download [BESmoothFontGen](https://github.com/KabanFriends/BESmoothFontGen/releases/latest) and extract the downloaded ZIP file
3. Download [msdfgen](https://github.com/Chlumsky/msdfgen/releases/latest) and extract the downloaded ZIP file
4. Place `msdfgen.exe` in the `msdfgen` folder inside the extracted BESmoothFontGen folder
5. Open `config.json` in a text editor and edit the file to include settings listed in the [Configuration](#configuration) section
6. Place the font files specified in the configuration in the `fonts` folder
7. Run the `BESmoothFontGen.bat` script to start the smooth font generation
8. Generated textures and data will be placed in the `smooth` folder

### Linux
The installation and usage is mostly same as Windows, however you must compile msdfgen binary from the source code by yourself. See [Chlumsky/msdfgen](https://github.com/Chlumsky/msdfgen) for more information.  
Generating the smooth font on Linux is several times faster than on Windows.

## Configuration
### Example configuration
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

### Settings
- `threads`: Numbers of threads to use when generating the font texture.
- `range`: Unicode character range to generate the smooth font of. If you want to generate 3 texture files ranging from Unicode 0x0000 to 0x02FF, you type `00` in the `from` field, and `02` in the `to` field.
- `fonts`: An array of fonts to use when generating the smooth font. When generating each character, this tool will first look at the topmost font in the array. If the character is not included in that font, it will check next fonts until the character is found.
    - `file`: Name of the font file. TTF and OTF font formats are supported, and font files defined here must be located in the `fonts` folder.
    - `size`: Size of the character to render in points.
    - `padding`: Amount of extra padding to add to the width of each character in this font.
    - `additionalArgs`: Additional parameters passed to the msdfgen process when generating the font texture. See [Chlumsky/msdfgen](https://github.com/Chlumsky/msdfgen) for more information.
- `cellMapping` (optional): Redirects characters to other characters' texture cells through `remapping.dat`. Either an inline array, or the path of a JSON file whose `mappings` property holds the array:
    ```json
    {
        "mappings": [
            { "char": "0410", "cell": "0041" },
            { "char": "2660", "cell": "0410" }
        ]
    }
    ```
    Each entry makes `char` use the texture **and width** stored in the cell of `cell`. Code points are hexadecimal.
    - When a character is mapped away from its own cell, that cell is free. The lowest character mapped into it is drawn there instead (above, U+2660 ♠ is drawn in U+0410's cell); if nothing is mapped into it, the cell is left empty.
    - Every other character mapped into a cell must render exactly like the cell's occupant (same traced glyph, render parameters and width). This is checked before generation, and the generator stops if any pair would differ.
    - `cell` must be on a page the client loads, and a character can only be mapped once.
- `pageRemapping` (optional, legacy): Draws a whole page into another page's cells. Entries are expanded into the same per-character mapping as `cellMapping`; the cells of the target page cannot also be used by `cellMapping`.
- `remapHangulChars` (optional, legacy): With `pageRemapping`, also redirect the target page's own characters to U+FFFE when the target page is in the Hangul Syllables range. Other target pages are always redirected.

## Compiling
You can compile BESmoothFontGen yourself with the following command:
```shell
./gradlew build
```
The compiled jar file will be placed in the `build/libs` folder.

## Smooth Font Specification
This section contains the unofficial documentation of the smooth font format used by Minecraft: Bedrock Edition. Smooth font format is not officially documented, and this format may be changed in future versions of the game.

### Overall structure
Both the font texture and the width data are separated in multiple pages, where each page contains data for 256 characters in the Unicode table.
Files are named in the format of `smooth_XX.(png|fontdata)` where the XX is the first two digits of the Unicode codepoint in hexadecimal.

### Texture
Font textures are stored as PNG images in the `font/smooth` folder of the resource pack. The texture dimension is 1024x1024 pixels, with the 16 rows, 16 columns grid of 64x64 glyph textures.
Each glyph texture uses the MTSDF format, a combination of MSDF and true SDF in the alpha channel.

### Width
Character widths are stored in binary files with the extension of `fontdata` in the `font/smooth` folder of the resource pack.
The first 4 bytes in the file are all 0x00. Then, for each character in the page, the file writes little-endian 32-bit floats between 0.0 and 1.0 where the width of 1.0 the full 64 pixels of the glyph grid.

### Remapping
`remapping.dat` in the `font/smooth` folder redirects characters to the texture cell of another character. The file is a list of 4-byte entries, each made of the source and the target code point as little-endian 16-bit integers.
A remapped character uses both the texture and the width of the target cell, and remaps are not followed in chains.
