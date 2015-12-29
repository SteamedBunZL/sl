if not exist build md build
cd /d D:\liu\rgame\Shenliao2.1_Ant

set /p x=请输入产品版本号(3.9.1):
set /p vno=请输入verNo(30901):
set /p vcod=请输入versioncode(391):
set /p bno=请输入buildNo(1):
set /p min=最小id值是:

set /p max=最大id值是:
 
call ant -f build.xml -Dversion.name =%x% -Dversion.code =%vcod%




for /l %%i in (%min%,1,%max%) do ant -f buildsign.xml -Dversion.name=%x%  -Dversion.buildNo=%bno% -Dversion.verNo=%vno% -Dversion.tuixinver=%vno% -Dversion.apptype=%x% -Dapp.id=%%i

set /p x=结束了:
pause