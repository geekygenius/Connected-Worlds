xcopy desktop\build\libs "C:\GithubIO\geekygenius.github.io\emergence" /Y /E
cd "C:\GithubIO\geekygenius.github.io\"
git add --all
git commit -m "Auto publish"
git pull
git push origin master
cd "C:\Users\Melissa\java_workspace\LD30\LD30"