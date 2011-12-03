Module description:
The ddi-3-xmlbeans is a set of Java wrappers for the DDI 3.x specification
used by other modules in the ddieditor framework.  

Legal: 
Copyright 2009 Danish Data Archive (http://www.dda.dk) 
 
For all files within ddi-3-xmlbeans: 

Ddi-3-xmlbeans is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ddi-3-xmlbeans is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with ddi-3-xmlbeans. If not, see <http://www.gnu.org/licenses/>.

Building:
The project uses an ANT build file
Buildfile: build.xml

Main targets:
 clean    Clean project
 init     Set up project
 javadoc  Compile javadoc for compiled schema code
 scomp    Compiles a schema into XML Bean classes and metadata
 
Default target: scomp

Setup:
1. The project 'lib-java' holding the XmlBeans jars needs to be checked out from source 
   version control as weel at: ../[project_home]
2. The project needs internet access to checkout the DDI schemas from the DDI source 
   version control at SourgeForge
2a.Waiting DDI-TIC to put ddi-3.1 into the repository at sf.net Until then download the 
	ddi-3.1 schema file into the location defined in build.properties waiting.tic.fix.localcopy.schema.dir         
3. Execute in shell at [project_home] ant to run ANT target 'scomp'

// ant properties
See: http://xmlbeans.apache.org/docs/2.0.0/guide/antXmlbean.html for additional 
ANT task scomp settings

Usage:
When using the generated classes refer to the generated jar:
[project_home]/xmllib/org.ddialliance.ddi3.xml.xmlbeans.jar because it 
includes some XmlBean specific filemappings from Java to XML schema. 

