#  Atari 2600: Bitmap to Playfield

### A tool to convert a BMP file to a playfield ASM listing for use in Atari 2600 games

Build your playfields with ease!  The tool accepts a bitmap file, in either 24-bit (RGB) or 32-bit 
(RBGA) format, and outputs an assembly file that can be dropped in to your project.  It outputs in 
the same format as [atari-background-builder](https://alienbill.com/2600/atari-background-builder/) 
where there are different sections (`PF0DataA`, `PF1DataA`, and `PF2DataA`) with one byte per row in
binary format.  It also builds the lists in reverse order (with the top of the screen at the bottom
of the list) for use in standard `dex`/`dey` loops.

## Usage

`java -jar a2600-bmp2pf.jar -f [input BMP file] -o [output ASM file] [any optional parameters]`

### Required parameters

* `-f`, `--file <arg>` : Path to input file.  File must in either 24-bit or 32-bit BMP format.  When in 32-bit format pixels that are fully transparent become 0, all others become 1. 32-bit format is recommended.
* `-o`, `--out <arg>` : Path to output file.  If file already exists it will be overwritten.

### Optional parameters
* `-b`, `--buffer <arg>` : Add a buffer of empty rows to the output file, where the supplied argument is the number of rows.
* `-x`, `--full-scale` : Normally the input BMP file is 1-to-1 - each pixel in the file is one pixel on the screen.  But the Atari 2600 horizontal playfield resolution is 1 bit is 4 pixels wide.  By using the `-x` option your input file is assumed to do the same - every 4 pixels in the file corresponds to 1 bit in a PF register.
* `-s`, `--symmetrical` : Generate a file for a symmetrical playfield.  An input BMP file of width 20 is required (or 80 if `-x` option is used).  This is the default.
* `-a`, `--asymmetrical` : Generate a file for an asymmetrical playfield.  An input BMP file of width 40 is required (or 160 if `-x` option is used).
* `-r`, `--repeated` : When generating a file for an asymmetrical playfield assume the input file is in the format "PF0 PF1 PF2 PF0 PF1 PF2", meaning the first 4 pixels are for PF0, the next 8 for PF1, the next 8 for PF2, the next 4 for PF0, etc.  This is the default if `-a` is specified but `-m` is not.
* `-m`, `--mirrored` : When generating a file for an asymmetrical playfield assume the input file is in the format "PF0 PF1 PF2 PF2 PF1 PF0", meaning the first 4 pixels are for PF0, the next 8 for PF1, the next 8 for PF2, the next 8 for PF2, etc.
