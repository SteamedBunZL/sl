if not exist build md build
cd /d D:\liu\rgame\Shenliao2.1_Ant

set /p x=�������Ʒ�汾��(3.9.1):
set /p vno=������verNo(30901):
set /p vcod=������versioncode(391):
set /p bno=������buildNo(1):
set /p min=��Сidֵ��:

set /p max=���idֵ��:
 
call ant -f build.xml -Dversion.name =%x% -Dversion.code =%vcod%




for /l %%i in (%min%,1,%max%) do ant -f buildsign.xml -Dversion.name=%x%  -Dversion.buildNo=%bno% -Dversion.verNo=%vno% -Dversion.tuixinver=%vno% -Dversion.apptype=%x% -Dapp.id=%%i

set /p x=������:
pause