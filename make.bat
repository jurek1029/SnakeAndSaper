set outdir=outd
mkdir %outdir%
mkdir %outdir%/resources
mkdir %outdir%/META-INF

javac -cp .;* -d %outdir% src/*.java
xcopy src\resources %outdir%\resources /syi
xcopy src\META-INF %outdir%\META-INF /syi
jar cvfm SnakeAndSapper.jar src/META-INF/MANIFEST.MF -C %outdir% .

pause