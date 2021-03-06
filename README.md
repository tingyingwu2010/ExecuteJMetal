<html>
<h1> ExecuteJMetal </h1>
Bash Script to help statistical tests of Multi-Objective Evolutionary Algorithms.<br>
This Scripts use <a href="https://jmetal.github.io/jMetal/">JMetal Framework 5.2</a>.<br>
Only for Linux Users.<br>
Include another two experiment components for JMetal, see in source code in java.<br>
Another component included in this project is the statistical comparisons, look at: <a href="https://github.com/LucasLP/ExecuteJMetal/tree/master/experiment/MyExperiments/comparison/examples"> "experiment/MyExperiments/comparison/examples" </a><br>
<i>But this component can be old, please see <a href="https://github.com/LucasLP/Multi-Objective-Algorithms-Comparison">this</a> repository.</i>

1. Use the src in additional files to programming your JMetalMain<br>
 Look at the end of this files for more informations.<br>
  
2. Install some programs<br>
	2.1. You need <b>LaTeX</b> - For .tex files<br>
<code>sudo apt-get install texlive-full</code><br>
<code>sudo apt install texlive-latex-base</code><br>
<code>sudo apt install texlive-generic-extra</code><br>
<code>sudo apt-get install texlive-latex-extra</code><br>
<code>sudo apt-get install texlive-science</code><br>


	2.2. You need <b>R Language</b> - for .R files<br>
<code>sudo apt-get update</code><br>
<code>sudo apt-get install r-base</code><br>
<code>sudo apt-get install r-base-dev</code><br>

	2.3. Install <a href="http://iridia.ulb.ac.be/irace/">Irace:</a><br>
     <code>R</code><br>
       <code>install.packages("irace") </code>
	<br>
	2.4. Install <a href="https://cran.r-project.org/web/packages/scatterplot3d/index.html">Scatterplot3D</a>, used in new experiment components:<br>
     <code>R</code><br>
		<code>install.packages("scatterplot3d", repos="http://R-Forge.R-project.org") </code><br>
	2.5. Install <a href="https://cran.r-project.org/web/packages/PMCMR/PMCMR.pdf"> PMCMR </a><br>
		<code>install.packages("PMCMR")</code><br>
	2.6. (optional) Install pdftk, used to merge PDFs<br>
	<code>sudo apt-get install pdftk</code><br>

3. Configure Execute.sh<br>
	3.1. Set Benchmark<br>
	3.2. Set Algorithm and its Tag (tag is the name of data files)<br>
	3.3. Set Execution line<br>
	3.4. Set Comparative Line<br>
    
4. Execute<br>
<code>sh Execute.sh</code><br>

5. Analyse the results in folder: experiment/MyExperiments/Result_$experimentName_$benchmark/

<hr>

<br>
<b>In Additional Folder</b><br>
<b>Script:</b> renameAll.sh<br>
Call the renameScript.sh for rename data files<br>
It be useful if you had old versions of JMetal and update, then, <br>
all of your data files will be for example "FUN.0", and the new versions use "FUN0.tsv"<br>
If you have this problem, set in this script algorithm and instance to rename.<br>
<br>
<b>Script:</b> renameScript.sh<br>
Rename All files in ".0 .1 .2  ...  .max" to ".tsv".<br>
<br>
<br>
<b>Source:</b> How to read input parameters to execute JMetal with this scripts.<br>
"MyRunner.java" is a single runner of algorithm configured by "Configuration.java"<br>
"ExecuteExperiment.java" can execute several runs and get all data for statistical test<br>
"Configuration.java" configure all tests, algorithms and benchmark<br>
"JMetalMain.java" read first argument to configure how it will execute<br><br>
<b>Example of execution: </b><br>
<code>$ java -jar JMetal.java --statistic ZDT --algorithm NSGAII --algorithm MOEAD --tag test</code><br>
It will execute NSGAII and MOEAD algorithm in benchmark ZDT, and MOEAD will save its data in "test" folder.<br>
<br>
<code>$ java -jar JMetal.java --single-run ZDT1 --algorithm MOEAD</code><br>
It will execute MOEAD in ZDT1 instance and print the quality indicator at console.<br>
<br>
<code>$ java -jar JMetal.jar --comparative UF --algorithm MOEAD --algorithm MOEADDRA</code><br>
It will generate latex and R files of statistical comparisons using  existing data files.<br>

<br>
<code>$ java -jar JMetal.jar --indicators UF --algorithm MOEAD</code><br>
It will execute the indicators of existing data files.<br>


<hr>
<br>
<b>Structure of tests</b><br>
<img src="Additional/Diagram.jpeg"><br>

  <ul>
    <li>Execute.sh</li>
    <li>QualityIndicator.sh</li>
    <li>JMetal.jar <i>Put You'r compilation here</i></li>
    <li>Additional/ </li>
      <ul>
      <li>renameAll.sh</li>
      <li>renameScript.sh</li>
		</ul>
    <li>src/</li>
       <ul>
        <li>Configuration.java</li>
        <li>ExecuteExperiment.java</li>
        <li>JMetalMain.java</li>
        <li>MyRunner.java</li>
       </ul>
   <li>lib/ <i>Lib of you'r project</i></li>
	<li>irace/</li> <i>Irace example using you compilation</i>
   <li>experiment/</li>
    <ul>
      <li>MyExperiments/</li>
      <ul>
        <li>data/</li>
      </ul>
    </ul>
 </ul>
</html>


<hr>
The "GenerateEvolutionChart.java" and "HistoryData.java" are the classes to generate this type of plot:
<img src="Additional/example_UF1_HV.png"><br>
For use this module, you need follow this steps:<br>
<ul>
	<li>In your algorithm: implement "HistoricAlgorithm", for example look MOEADDRA in this src files;</li>
	<ul>
		<li>Add an Map of "String" to "HistoryData" and add the classes for each indicators;</li>
	</ul>
	<li>At each evaluation test and calculate the quality indicator;</li>	
	<ul>
		<li>You can use the static methods of "HistoricAlgorithm", only call this methos like the implementation in MOEADDRA in this src;</li>
	</ul>
	<li>At final, Print Historic data, for example look "ExecuteExperiment"</li>
	<li>Generate Rscript with "GenerateEvolutionChart"</li>
</ul>
<i>Obs.: Run it before the run of other Experiment Components (generate of tables and scripts) because these components can modify the experiment algorithm, then you should have an exception;</i>

<hr>

The Scatter Plot of point can be found at "experiment/MyExperiments/comparison/functions.R".<br>
You can import the scripts and use, like:
<code>R</code><br>
<code>source("functions.R")</code><br>
<code>algorithms = c("MOEADDRA","NSGAII","IBEA")</code><br>
<code>objectivePoints("UF7", algorithms)</code><br>
<code>objectivePoints3D("UF8", algorithms)</code><br>

<br>
<img src="Additional/scatter-Plot.png"><br>


<hr>
<b>Step by Step: How to use</b>

<ol type="1">
  <li>Clone this project;</li>
  <li>Copy src of JMetalMain, Configuration, ExecuteExperiment, MyRunner to your project and put your algorithm;</li>
  <ol type="1">
  	<li>copy MOEAD, MOEADDRA, MOEADBuilder, NSGAII and IBEA</li>
  </ol>
  <li>Configure the initialization of your algorithm in Configuration.java;</li>
  <li>Build your JMetal.jar, and put in this directory;</li>
  <li>Configure the algorithms and benchmark to run in Execute.sh</li>
  <li>Execute it!</li>
  <li>The Results are in "experiment/MyExperiments"</li>
</ol>  


