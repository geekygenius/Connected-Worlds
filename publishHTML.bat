xcopy html\build\dist "C:\GithubIO\geekygenius.github.io\dist" /Y /E
cd "C:\GithubIO\geekygenius.github.io\dist"
git add --all
git commit -m "Auto publish"
git push origin master
cd "C:\Users\Melissa\java_workspace\LD30\LD30"