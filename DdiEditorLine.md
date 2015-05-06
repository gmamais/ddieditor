# DdiEditor Line #
DdiEditor Line is a plug-in encapsulating the feature to script DDI-L elements in [Mediawiki format](http://www.mediawiki.org/wiki/Help:Formatting) acting as an import tool for the DdiEditor platform. The DDI-L supported elements are devided in two groups. DDI-L elements directly scripted in [Mediawiki format](http://www.mediawiki.org/wiki/Help:Formatting) and DDI-L elements that are indirectly created.



## Directly specified DDI-L Elements ##
The following section describes the directly specified DDI-L elements and their Mediawiki syntax.

### Universe ###
When a universe is defined the following defined questions items are related to it.
```
=Universe Label=Universe Description
```

### Sequence ###
A sequence is defined by a start and a end tag enclosing all DDI-L elements supported by the DdiEditor Line module export for sequences.
```
==[seq-id]==Sequence Label Text
...
==end==
```
Note: seq-id is the identification of the sequence. The seq-id must not start by 'V' or 'v'. A sub-sequence will normally be referred to by one or more ifthenelse elements. A sub-sequence must not include another sub-sequence.
Example:
```
==seq-a==Question V10, V11 and V12
* V10 Question Item
* V11 Question Item
''ifthenelse'' V10==2 V12 na Please given a answer to question V12
* V12 Question Item
==end==
```

### Question Scheme ###
When a question scheme is defined the following defined questions items using it as their maintainable element.
```
===Question Scheme Label===Question Scheme Description
```

### Question Item ###
```
* V[vari number] Question text
```
Note, vari number is the variable identifier that the question item relates to. If a question item has non or more variable relation '''na''' as variable number, may be specified to skip the relation to a variable. Specificly variable number has to be the same unique content defined in a variable name element within the same resource to define a question item and variable relation.

### Category ###
Categories can be defined or a whole category scheme can be referred.

**Defined:**
```
** Category text
```

**Referred:**
```
** [vari number]
```
Eg:
```
** V12
```
Vari number refers to the question item related to a variable. The referred category scheme is the scheme associated with the code scheme of the variable.

### Multiple Question Item ###
A multiple question item is defined by a start and end enclosing question items.
```
'''[#Group ID] Multiple Question Item Text'''
* V10 Question Item
* V11 Question Item
'''end'''
```
A set of Multiple Question Items may be grouped by giving a Group ID. A Group ID has to start with a '#' character. The Group ID is terminated by a space character. A Group ID is optional.

Example:
```
'''#A1 Question Item Text 1'''
* V1 Question 1.
* V2 Question 2.
'''end'''

'''#A1 Question Item Text 2'''
* V3 Question 3.
* V4 Question 4.
'''end'''
```
'A1' is the Group ID.
### Statement Item ###
A statement item is defined by
```
''state'' Statement Item Text
```

### Computation Item ###
A computation item is defined by
```
''comp'' [boolean expression] [vari number] Label Text
```

**Boolean expresssion** specifies the condition accociated with one to many values of variables and the following expressions:
  * > greater than
  * >= equals or greater than
  * < lesser than
  * =< equals or greater than
  * == equals
  * && AND
  * || OR

**Vari number** refers to the related variable.

### Interviewer Instruction ###
An interviewer instruction supersedes a question definition and a reference from the question construct is created to the **preceding** interviewer instructions.

An interviewer instruction is defined by
```
 ''interview'' Interviewer Instruction Text [boolean expression]||[na|NA]
```
''Boolean expresssion'' specifies the condition accociated with one to many values of variables and the following expressions:
  * > greater than
  * >= equals or greater than
  * < lesser than
  * =< equals or greater than
  * == equals
  * && AND
  * || OR

''NA'' specifies not applicable and can be both upper and lower case.Notice that the ''Boolean expresssion'' or ''NA'' can be empty thus for convenience the following is valid too.
```
 ''interview'' Interviewer Instruction Text
```

Example:
```
 ''interview'' Please, tick only one box.
 ''interview'' Please, tick minimum two boxes NA
 ''interview'' Please, only tick one box v7>8
 *V16 Spm. 4. Do you have to meet a fixed deadline?
 **No
 **Yes
```

### If Then Else ###
The Control Construct IfThenElse element/ filter.
```
''ifthenelse'' [boolean expression] [then vari number | then seq-id ] [else vari number | else seq-id] Statement Item Text
```

**Question ref** Is the reference to the if condition question

**Boolean expresssion** specifies the condition accociated with one to many values of variables and the following expressions:
  * > greater than
  * >= equals or greater than
  * < lesser than
  * =< equals or greater than
  * == equals
  * != NOT equals
  * && AND
  * || OR

**Then vari number** refers to the then question item related to a variable.
<br><br>
<b>Else vari number</b> refers to the else question item related to a variable. The else reference can be <b>na</b>.<br>
<br>
<pre><code>''ifthenelse'' V2&gt;6&amp;&amp;V12==2 V16 na We would like to ask you some more questions regarding online gaming.<br>
</code></pre>

<h2>Indirectly created DDI-L Elements</h2>
<h3>Concept</h3>
When a question scheme is defined the question items residing within the question schme are related to a created concept have the same label as the question schme.<br>
<br>
<h3>Category Scheme</h3>
When one to many categories under a question item are defined they are residing with in a constructed category scheme with a label representing the categories.<br>
<br>
<h3>Question Construct</h3>
When a question item is defined a corresponding question construct is defined.<br>
<br>
<h3>Sequence</h3>
All question items, multiple questions and if then else constructs are added by reference to a sequence reflecting the logic flow of the survey.<br>
<br>
<h3>Universe</h3>
When an if then else construct is defined the then question item reference is releated to a constructed universe reflecting the filter.