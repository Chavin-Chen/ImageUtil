package com.chavin.util.image

/**
 * java -jar image.jar
 */
class Main {
    private static IS_DEBUG = false
    private static String input = ''
    private static List<File> inputFiles = []
    private static String output = ''
    private static File outputFile = null
    private static def scale = ICompressor.DEF_SCALE
    private static def quality = ICompressor.DEF_QUALITY
    private static int engine = 0
    private static def suffix = ''
    private static def filter = new FilenameFilter() {
        @Override
        boolean accept(File dir, String name) {
            return acceptFile(name)
        }
    }

    static void main(String[] args) {
        if (null == args || args.length <= 1 || args[1] == '--help' || args[1] == '-h' || args[1] == '?') {
            printHelp()
            return
        }
        long started = System.nanoTime()
        try {
            int cnt = run(args)
            if (0 != cnt) {
                long cost = System.nanoTime() - started
                println "Processed $cnt files cost ${cost / 1E9} seconds"
                started = 0L
            }
        } catch (RuntimeException e) {
            if (debug()) {
                e.printStackTrace()
            }
        }
        if (0L != started) {
            println 'Error input, you need may need the usage'
            printHelp()
        }
    }

    private static int run(String[] args) {
        debug("run: ${Arrays.toString(args)}")

        def list = []
        for (String arg : args) list << arg
        parseArgs list

        if (0 == Double.compare(scale, ICompressor.DEF_SCALE) && 0 == Double.compare(quality, ICompressor.DEF_QUALITY)) {
            println "Need do nothing."
            return 0
        }
        if (input.blank) {
            println "No input."
            return 0
        }
        parseInputFile()
        if (inputFiles.empty) {
            println "No input file"
            return 0
        }
        if (!output.blank) {
            outputFile = new File(output)
        }
        ICompressor compressor = (engine == 0 ? new CustomCompressor() : new ThumbnailCompressor())
        if (inputFiles.size() == 1) {
            if (null == outputFile || !outputFile.isFile()) {
                outputFile = new File(inputFiles[0].parentFile, getDstFileName(inputFiles[0]))
            }
            if (outputFile.exists()) outputFile.delete()
            if (0 == Double.compare(scale, ICompressor.DEF_SCALE)) {
                debug("run:compress ${inputFiles[0]} to $outputFile")
                compressor.compress(inputFiles[0], outputFile, quality)
            } else {
                debug("run:scale ${inputFiles[0]} to $outputFile")
                compressor.scale(inputFiles[0], outputFile, scale, quality)
            }
            return 1
        }
        if (null == outputFile) {
            outputFile = inputFiles[0].parentFile
        }
        if (!outputFile.isDirectory()) {
            outputFile = outputFile.parentFile
        }
        debug("run:output folder:$outputFile")
        outputFile.mkdirs()
        def targetFile
        for (def file : inputFiles) {
            targetFile = new File(outputFile, getDstFileName(file))
            if (targetFile.exists()) targetFile.delete()
            if (0 == Double.compare(scale, ICompressor.DEF_SCALE)) {
                debug("run:compress $file to $targetFile")
                compressor.compress(file, targetFile, quality)
            } else {
                debug("run:scale $file to $targetFile")
                compressor.scale(file, targetFile, scale, quality)
            }
        }
        return inputFiles.size()
    }


    private static parseArgs(List<String> args) {
        String arg
        for (int i = 0; i < args.size();) {
            arg = args[i]
            if (arg.startsWith('--input')) {
                input = arg.split('=', 2)[1]
                ++i
                continue
            }
            if (arg == '-i') {
                input = args[++i]
                // Bash上短参数通配符会在shell层展开
                int j
                for (j = i; j < args.size(); j++) {
                    if (args[j].startsWith('-')) {
                        if (j == i) ++j
                        break
                    }
                    inputFiles << new File(args[j])
                }
                i = j
                continue
            }
            if (arg.startsWith('--output')) {
                output = arg.split('=', 2)[1]
                ++i
                continue
            }
            if (arg == '-o') {
                arg = args[++i]
                output = arg
                ++i
                continue
            }
            if (arg.startsWith('--quality')) {
                quality = Double.parseDouble(arg.split('=', 2)[1])
                ++i
                continue
            }
            if (arg == '-q') {
                quality = Double.parseDouble(args[++i])
                ++i
                continue
            }
            if (arg.startsWith('--scale')) {
                scale = Double.parseDouble(arg.split('=', 2)[1])
                ++i
                continue
            }
            if (arg == '-s') {
                scale = Double.parseDouble(args[++i])
                ++i
                continue
            }
            if (arg.startsWith('--engine')) {
                arg = arg.split('=', 2)[1]
                engine = ((arg == 'thumb' || arg == 't') ? 1 : 0)
                ++i
                continue
            }
            if (arg == '-e') {
                arg = args[++i]
                engine = ((arg == 'thumb' || arg == 't') ? 1 : 0)
                ++i
                continue
            }
            if (arg.startsWith('--suffix')) {
                suffix = arg.split('=', 2)[1]
                ++i
                continue
            }
            if (arg == '--debug' || arg == '-d') {
                IS_DEBUG = true;
            }
            ++i
        }
        debug("parseArgs: input=$input, output=$output")
        debug("parseArgs: quality=$quality, scale=$scale, engine=$engine suffix=$suffix")
    }

    private static parseInputFile() {
        if (input.contains('*')) { // May be: /* 、/*.* 、/*.png
            String[] splits = input.split('\\*', 2)
            debug("parseInputFile(*):${Arrays.toString(splits)}")
            File folder = new File("${splits[0]}".replace('~', System.getProperty("user.home")))
            if (splits.length > 1) {
                inputFiles.addAll((File[]) folder.listFiles(new FilenameFilter() {
                    @Override
                    boolean accept(File dir, String name) {
                        if (splits[1].endsWith('*')) {
                            return acceptFile(name)
                        }
                        return name.endsWith(splits[1])
                    }
                }))
            } else {
                inputFiles.addAll((File[]) folder.listFiles(filter))
            }
        } else if (inputFiles.empty) {
            def file = new File(input)
            debug("parseInputFile(n): $file  ${file.exists()}")
            if (file.isFile()) {
                inputFiles << file
            } else {
                inputFiles.addAll((File[]) file.listFiles(filter))
            }
        }
    }

    private static boolean acceptFile(String fName) {
        // 后缀判断，因为webp的头太复杂了，再者ImageIO也需要用，若弄个映射太麻烦。工具么~太重了用起来吃力
        return fName.endsWith('.png') || fName.endsWith('.webp') || fName.endsWith('.jpg') || fName.endsWith('.jpeg')
    }

    private static getDstFileName(File srcFile) {
        if (suffix.blank) return srcFile.name
        String[] splits = srcFile.name.split('\\.')
        return "${splits[0]}$suffix.${splits[1]}"
    }

    private static printHelp() {
        println(
                '''
This is a image compress tool(support *.jpg、*.png、*.webp). Usage:

    java -jar image.jar <arguments>

About arguments:

    --input=<file>, -i <file>    Set the input folder(like '~/img/'), files(like '~/img/*.*'
                                     or '~/img/*.png') or file(like '~/img/1.webp')
    --output=<file>, -o <file>   Set the output folder(like '~/img2/') or file(like 
                                     '~/img2/1.webp'). default '.'(input folder)
    --quality=0.75, -q 0.75      Set the quality factor of image compressor, valued in 
                                    (0.0, 1.0], default 1.0
    --scale=0.8, -s 0.8          Set the scale ratio of image, valued in (0.0, MAX), 
                                    default 1.0
    --engine=custom, -e custom   Set the compressor engine, default engine is custom.
                                    custom engine(short 'c') based ImageIO and Graphics2D
                                    thumb engine(short 't') based Google Thumbnails library
    --suffix=_opt                Set the dest file suffix, for example value means
                                    ~/img/1.webp to ~/img2/1_opt.webp. default ''
    --debug, -d                  Set debug mode for more logs
''')
    }

    private static boolean debug(String line) {
        if (!IS_DEBUG) return false
        if (null != line) {
            println(line)
        }
        return true
    }
}
