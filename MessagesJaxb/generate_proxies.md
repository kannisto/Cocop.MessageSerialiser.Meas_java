Generation of XML proxy classes
===============================

Author: Petri Kannisto, Tampere University, Finland  
Last modified: 4/2020


Assumptions in this file
------------------------

It is assumed that you run Java in Windows. However, it is likely that the
XJC tool like works similarly in all environments.

Another assumption is that "xjc.exe" is located in path
"C:\Program Files\Java\jdk8\bin\xjc.exe". Change the path in each command if
your system has the tool in another location.


Steps
-----

1. Choose a working directory other than this application to prevent any
   accidental overwrites.

2. Copy the following files into the working directory.
    * proxyhelper.xsd
    * bindings.xml
    * The folder "Schemata\cocopcustom" (contains "cocopcustom_1.1.xsd"
      and "cocopcustom_1.2.xsd")

3. In the working directory, run the following command. The execution will
   take some time. 

```
"C:\Program Files\Java\jdk8\bin\xjc.exe" proxyhelper.xsd -b bindings.xml
```

4. Copy each generated folder to "MessagesJaxb/src" (in this project). That
   is, you will have the following folder structure.

```
MessagesJaxb/src/eu/...
MessagesJaxb/src/net/...
MessagesJaxb/src/org/...
```

5. In file "eu\cocop_spire\om_custom\_1_2\ArrayType.java", modify the lines
   above class declaration as follows. Also add the required import for
   _XmlRootElement_.

```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayType", propOrder = {
    "row"
})
@XmlRootElement (name="Array") // Manually added!
public class ArrayType {
```

6. Change file "eu\cocop_spire\om_custom\_1_2\ArrayRowType.java" as follows.
   Update the related getter respectively.

```
protected List<Object> i; -> protected List<String> i;
```

7. Change file "net\opengis\tsml\_1\TimePositionListType.java" as follows.
   Update the related getter respectively.

```
protected List<XMLGregorianCalendar> timePositionList -> protected List<String> timePositionList;
```
