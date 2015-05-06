

# Installation #
## Check Java Version ##
  1. Check that you have Java version 1.6 or higher installed:
  1. Open a DOS promt/ shell and execute:
```
java -version
```

## Installation on Windows ##
### Prerequisites ###
  1. This user has to have administrator privilege on the PC.

### Install Oracle Berkeley DB XML ###
  1. **Download** [Oracle Berkeley DB XML](http://www.oracle.com/technology/software/products/berkeley-db/xml/index.html),  version 2.5.16. For convenience use the [Windows installer ](http://download.oracle.com/otn/berkeley-db/dbxml-2.5.16.msi)
  1. Set an environment variable **ORACLE\_BERKELEY\_DB\_XML\_HOME** to the installation directory of Oracle Berkeley DB XML e.g. C:\Programmer\Oracle\Berkley DB XML 2.5.16
  1. First check if the enviroment variables have been created by the Windows installer, by executing in a DOS prompt:
```
echo %ORACLE_BERKELEY_DB_XML_HOME%
echo %PATH%
```
  1. **Create environment variable**
Start -> Settings -> Control Panel -> System
Select: "Advanced tab"
Select "Enviroment Variables"

Create new user variable select
New Variable: ORACLE\_BERKELEY\_DB\_XML\_HOME
Value: Oracle Berkeley DB XML install directory e.g. C:\Programmer\Oracle\Berkeley DB XML 2.5.16

**Windows comment:** When changing Oracle Berkeley version - remove path to old version in user environment variable PATH

5. Check that the **USER**->**PATH** variable is set and pointing to **%ORACLE\_BERKELEY\_DB\_XML\_HOME%\bin**

### Install DdiEditor ###
  1. Get DdiEditor from **[Downloads](https://drive.google.com/?authuser=0#folders/0B8DmOLm957BPUmx3elhfTXZiNEE)** choose the latest ddieditor-x.x.x.win32.win32.x86.zip file
  1. Unzip the file into your program directory e.g. C:\Programmer<br>
<ol><li>Run the ddieditor.exe program in the installation directory.<br></li></ol>

<h2>Installation on Linux</h2>
The installation for Linux is tested on Debian based systems.<br>
<br>
<h3>Install Oracle Berkeley DB XML</h3>
<ol><li><b>Download</b> <a href='http://www.oracle.com/technology/software/products/berkeley-db/xml/index.html'>Oracle Berkeley DB XML</a>, version 2.5.16. For convenience use the <a href='http://download.oracle.com/otn/berkeley-db/dbxml-2.5.16.tar.gz'>Tar.Gz Archive</a>
<ol><li><b>Extract</b> dbxml-2.5.16.tar.gz to DBXML_DIR<br>
</li></ol></li><li>To work around <a href='https://wiki.edubuntu.org/GCC4.6'>GCC4.6 isssues</a> with XQilla in the DBXml-2.5.16 version <b>download</b> <a href='http://sourceforge.net/projects/xqilla/'>XQilla-2.3.0</a>
<ol><li><b>Extract</b> XQilla-2.3.0.tar.gz to XQILLA_DIR<br>
</li></ol></li><li><b>Build</b> DBXml with Java support and XQilla-2.3.0 sources<br>
<pre><code>cd DBXML_DIR<br>
./buildall.sh --enable-java --with-xqilla=XQILLA_DIR<br>
</code></pre>
</li><li><b>Wait</b> and watch the logs ...</li></ol>

<h3>Install DdiEditor</h3>
<ol><li>Get DdiEditor from <b><a href='https://drive.google.com/?authuser=0#folders/0B8DmOLm957BPUmx3elhfTXZiNEE'>Downloads</a></b> choose the latest ddieditor-x.x.x.linux.gtk.x86.zip file<br>
</li><li>Unzip the file into DDIEDITOR_DIR<br>
</li><li>By default DdiEditor will be looking for DBXml libaries at /opt/dbxml-2.5.16/install/lib if you do not wish to install DBXml here edit to suite the location of DBXML_DIR<br>
<pre><code>vi DDIEDIOTOR_DIR/ddieditor.ini<br>
</code></pre>
</li><li>Execute ddieditor in DDIEDITOR_DIR to launch the DdiEditor