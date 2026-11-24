import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Main {
 static final Path CF=Paths.get("courses.txt"), RF=Paths.get("registrations.txt");
 static Map<String,String> title=new HashMap<>(), names=new HashMap<>();
 static Map<String,Integer> capacity=new HashMap<>();
 static Map<String,List<String>> enrolled=new HashMap<>();
 static Map<String,Queue<String>> waiting=new HashMap<>();
 static Map<String,Set<String>> prereq=new HashMap<>();
 static Map<String,Set<String>> done=new HashMap<>();

 public static void main(String[] args)throws Exception {
  load();
  printBanner("COLLEGE COURSE REGISTRATION SYSTEM");
  System.out.println("  "+capacity.size()+" courses loaded  |  Registration data ready");
  Scanner in=new Scanner(System.in);
  while(true){
   printMenu();
    System.out.print("  Select an option [1-7]: "); String c=in.nextLine().trim();
    if(c.equals("6"))break;
   try{
    if(c.equals("1")){System.out.print("Student: ");String n=in.nextLine();
     System.out.print("Course: ");printResult(register(n,in.nextLine()));}
    else if(c.equals("2")){System.out.print("Student: ");String n=in.nextLine();
     System.out.print("Course: ");printResult(complete(n,in.nextLine()));}
    else if(c.equals("3")){System.out.print("Student: ");String n=in.nextLine();
     System.out.print("Course: ");printResult(drop(n,in.nextLine()));}
    else if(c.equals("4"))showCourses();
    else if(c.equals("5")){System.out.print("Student: ");showStudent(in.nextLine());}
    else if(c.equals("7"))runTestCases();
    else System.out.println("Choose 1 to 7.");
   }catch(Exception e){System.out.println(e.getMessage());}
  }
  in.close();save();System.out.println("Data saved. Goodbye.");
 }

 static void printBanner(String text){
  String line="=".repeat(66);
  System.out.println("\n"+line);
  System.out.println(center(text,66));
  System.out.println(line);
 }

 static void printMenu(){
  printBanner("MAIN MENU");
  System.out.println("  [1] Register for a course");
  System.out.println("  [2] Mark a course completed");
  System.out.println("  [3] Drop course or waiting-list entry");
  System.out.println("  [4] View course dashboard");
  System.out.println("  [5] View student registrations");
   System.out.println("  [6] Save and exit");
   System.out.println("  [7] Run built-in test cases");
  System.out.println("-".repeat(66));
 }

 static void printResult(String message){
  System.out.println("\n  "+message);
 }

 static String center(String text,int width){
  int left=(width-text.length())/2;
  return " ".repeat(Math.max(0,left))+text;
 }

 static String name(String s){
  if(s==null||s.trim().isEmpty()||s.contains("|"))
   throw new IllegalArgumentException("Enter a valid name.");
  return s.trim();
 }
 static boolean isEnrolled(String id,String s){
  return enrolled.get(id).stream().anyMatch(x->x.equalsIgnoreCase(s));
 }
 static boolean isWaiting(String id,String s){
  return waiting.get(id).stream().anyMatch(x->x.equalsIgnoreCase(s));
 }
 static int seats(String id){return capacity.get(id)-enrolled.get(id).size();}

 static List<String> missing(String id,Set<String> finished){
  List<String> answer=new ArrayList<>(),queue=new ArrayList<>();
  Set<String> seen=new HashSet<>();
  queue.add(id);seen.add(id);
  for(int i=0;i<queue.size();i++)
   for(String p:prereq.getOrDefault(queue.get(i),Collections.emptySet()))
    if(seen.add(p)){queue.add(p);if(!finished.contains(p))answer.add(p);}
  return answer;
 }

 static String register(String s,String id){
  s=name(s);id=id.trim().toUpperCase();
  if(!capacity.containsKey(id))return "Course "+id+" not found.";
  if(isEnrolled(id,s)||isWaiting(id,s))
   return s+" is already registered for "+id+".";
  Set<String> finished=done.getOrDefault(s.toLowerCase(),Collections.emptySet());
  List<String> missing=missing(id,finished);
  if(!missing.isEmpty())
   return "Finish these prerequisites first: "+String.join(", ",missing)+".";
  if(seats(id)>0){enrolled.get(id).add(s);return "Registration successful: "+s+" -> "+id;}
  waiting.get(id).add(s);return "Course full. "+s+" added to waiting queue.";
 }

 static String complete(String s,String id){
  s=name(s);id=id.trim().toUpperCase();
  if(!capacity.containsKey(id))return "Course "+id+" not found.";
  if(!isEnrolled(id,s))return s+" must be enrolled in "+id+" first.";
  names.put(s.toLowerCase(),s);
  done.computeIfAbsent(s.toLowerCase(),k->new HashSet<>()).add(id);
  return s+" marked "+id+" completed.";
 }

 static String drop(String s,String id){
  String student=name(s);id=id.trim().toUpperCase();
  if(!capacity.containsKey(id))return "Course "+id+" not found.";
  Iterator<String> i=waiting.get(id).iterator();
  while(i.hasNext())if(i.next().equalsIgnoreCase(student)){i.remove();return student+" left the waiting queue";}
  if(!enrolled.get(id).removeIf(x->x.equalsIgnoreCase(student)))
   return student+" is not registered for "+id+".";
  String next=waiting.get(id).poll();
  if(next==null)return student+" dropped "+id;
  enrolled.get(id).add(next);
  return student+" dropped "+id+". "+next+" got the seat.";
 }

 static void showCourses(){
  printBanner("COURSE DASHBOARD");
  System.out.printf("  %-8s %-30s %8s%n","COURSE","TITLE","SEATS");
  System.out.println("-".repeat(66));
  List<String> ids=new ArrayList<>(capacity.keySet());
  Collections.sort(ids);
  for(String id:ids){
   System.out.printf("  %-8s %-30s %4d / %-4d%n",id,title.get(id),seats(id),capacity.get(id));
   System.out.println("Enrolled: "+join(enrolled.get(id)));
   System.out.println("Waiting: "+join(new ArrayList<>(waiting.get(id))));
   System.out.println("-".repeat(66));
  }
 }

 static void showStudent(String s){
  s=name(s);boolean found=false;
  printBanner("STUDENT REGISTRATIONS");
  System.out.println("  Student: "+s);
  System.out.println("-".repeat(66));
  List<String> ids=new ArrayList<>(capacity.keySet());
  Collections.sort(ids);
  for(String id:ids){
   if(isEnrolled(id,s)){System.out.println(id+" - enrolled");found=true;}
   else if(isWaiting(id,s)){System.out.println(id+" - waiting");found=true;}
  }
  if(!found)System.out.println("No registrations for "+s+".");
 }
 static String join(List<String>a){return a.isEmpty()?"-":String.join(", ",a);}

 static void runTestCases(){
  printBanner("BUILT-IN TEST CASES");
  Map<String,String>oldTitle=new HashMap<>(title);
  Map<String,String>oldNames=new HashMap<>(names);
  Map<String,Integer>oldCapacity=new HashMap<>(capacity);
  Map<String,List<String>>oldEnrolled=new HashMap<>();
  for(String id:enrolled.keySet())oldEnrolled.put(id,new ArrayList<>(enrolled.get(id)));
  Map<String,Queue<String>>oldWaiting=new HashMap<>();
  for(String id:waiting.keySet())oldWaiting.put(id,new LinkedList<>(waiting.get(id)));
  Map<String,Set<String>>oldPrereq=new HashMap<>();
  for(String id:prereq.keySet())oldPrereq.put(id,new HashSet<>(prereq.get(id)));
  Map<String,Set<String>>oldDone=new HashMap<>();
  for(String key:done.keySet())oldDone.put(key,new HashSet<>(done.get(key)));

  resetTestData();
  runCase("Test 1: Normal registration",
   register("Aman","CS101").startsWith("Registration successful"));
  runCase("Test 2: Boundary / waiting queue",
   register("Riya","CS101").startsWith("Registration successful")
    && register("Karan","CS101").startsWith("Course full")
    && drop("Aman","CS101").contains("got the seat"));
  runCase("Test 3: Invalid input",
   register("Aman","C999").equals("Course C999 not found.")
    && !unknownName("").isEmpty());
  resetTestData();
  runCase("Test 4: Valid prerequisite scenario",
   register("Priya","CS102").startsWith("Finish these prerequisites")
    && register("Priya","CS101").startsWith("Registration successful")
    && complete("Priya","CS101").contains("completed")
    && register("Priya","CS102").startsWith("Registration successful"));

  title=new HashMap<>(oldTitle);names=new HashMap<>(oldNames);
  capacity=new HashMap<>(oldCapacity);
  enrolled=new HashMap<>();
  for(String id:oldEnrolled.keySet())enrolled.put(id,new ArrayList<>(oldEnrolled.get(id)));
  waiting=new HashMap<>();
  for(String id:oldWaiting.keySet())waiting.put(id,new LinkedList<>(oldWaiting.get(id)));
  prereq=new HashMap<>();
  for(String id:oldPrereq.keySet())prereq.put(id,new HashSet<>(oldPrereq.get(id)));
  done=new HashMap<>();
  for(String key:oldDone.keySet())done.put(key,new HashSet<>(oldDone.get(key)));
 }

 static void resetTestData(){
  title.clear();names.clear();capacity.clear();enrolled.clear();
  waiting.clear();prereq.clear();done.clear();
  title.put("CS101","Introduction to Programming");
  title.put("CS102","Data Structures");
  capacity.put("CS101",2);capacity.put("CS102",2);
  enrolled.put("CS101",new ArrayList<>());
  enrolled.put("CS102",new ArrayList<>());
  waiting.put("CS101",new LinkedList<>());
  waiting.put("CS102",new LinkedList<>());
  prereq.put("CS101",new HashSet<>());
  prereq.put("CS102",new HashSet<>(List.of("CS101")));
 }

 static String unknownName(String s){
  try{name(s);return "";}catch(Exception e){return e.getMessage();}
 }

 static void runCase(String label,boolean passed){
  System.out.println((passed?"PASS":"FAIL")+"  "+label);
 }

 static void load()throws Exception{
  starterFiles();
  List<String> lines=Files.readAllLines(CF,StandardCharsets.UTF_8);
  for(int i=0;i<lines.size();i++){
   String line=lines.get(i).trim();
   if(line.isEmpty()||line.startsWith("#"))continue;
   String[]f=line.split("\\|",-1);
   if(f.length!=4)throw new Exception("Bad courses.txt line "+(i+1));
   try{
    String id=f[0].trim().toUpperCase();
    if(capacity.containsKey(id))throw new RuntimeException("duplicate course");
    title.put(id,f[1].trim());
    capacity.put(id,Integer.parseInt(f[2].trim()));
    enrolled.put(id,new ArrayList<>());
    waiting.put(id,new LinkedList<>());
    prereq.put(id,new HashSet<>());
    if(!f[3].trim().isEmpty())
    for(String p:f[3].split(","))prereq.get(id).add(p.trim().toUpperCase());
   }catch(RuntimeException e){throw new Exception("Bad courses.txt line "+(i+1));}
  }
  lines=Files.readAllLines(RF,StandardCharsets.UTF_8);
  for(int i=0;i<lines.size();i++){
   String line=lines.get(i).trim();
   if(line.isEmpty()||line.startsWith("#"))continue;
   String[]f=line.split("\\|",-1);
   if(f.length!=3)throw new Exception("Bad registrations.txt line "+(i+1));
   String type=f[0].toUpperCase(),s=f[1].trim(),id=f[2].trim().toUpperCase();
   if(!capacity.containsKey(id))throw new Exception("Unknown course: "+id);
   if(type.equals("E")){
    if(seats(id)==0||!enrolled.get(id).add(s))throw new Exception("Invalid enrollment.");
   }else if(type.equals("W")){
    if(seats(id)>0||isEnrolled(id,s)||isWaiting(id,s))throw new Exception("Invalid waiting record.");
    waiting.get(id).add(s);
   }else if(type.equals("C")){
    names.put(s.toLowerCase(),s);
    done.computeIfAbsent(s.toLowerCase(),k->new HashSet<>()).add(id);
   }else throw new Exception("Unknown record type: "+type);
  }
 }

 static void save()throws Exception{
  List<String>lines=new ArrayList<>(),ids=new ArrayList<>(capacity.keySet());
  Collections.sort(ids);
  lines.add("# E|name|course, W|name|course, C|name|course");
  for(String id:ids){
   for(String s:enrolled.get(id))lines.add("E|"+s+"|"+id);
   for(String s:waiting.get(id))lines.add("W|"+s+"|"+id);
  }
  for(String key:done.keySet())
   for(String id:done.get(key))lines.add("C|"+names.get(key)+"|"+id);
  Files.write(RF,lines,StandardCharsets.UTF_8);
 }

 static void starterFiles()throws Exception{
  if(!Files.exists(CF))Files.write(CF,List.of(
   "# ID|title|capacity|prerequisites",
   "CS101|Introduction to Programming|2|",
   "CS102|Data Structures|2|CS101",
   "CS201|Algorithms|2|CS102",
   "MA101|Discrete Mathematics|2|"),StandardCharsets.UTF_8);
  if(!Files.exists(RF))Files.write(RF,List.of(
   "# E|name|course, W|name|course, C|name|course"),StandardCharsets.UTF_8);
 }
}
