import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
  if(GraphicsEnvironment.isHeadless()){consoleMain();return;}
  SwingUtilities.invokeLater(()->new RegistrationFrame().setVisible(true));
 }

 static void consoleMain()throws Exception{
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
  List<String> results=testCaseResults();
  for(String result:results)System.out.println(result);
 }

 static List<String> testCaseResults(){
  List<String> results=new ArrayList<>();
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
  try{
   resetTestData();
   results.add(testResult("Test 1: Normal registration",
    register("Aman","CS101").startsWith("Registration successful")));
   results.add(testResult("Test 2: Capacity and waiting queue",
    register("Riya","CS101").startsWith("Registration successful")
     && register("Karan","CS101").startsWith("Course full")
     && drop("Aman","CS101").contains("got the seat")));
   results.add(testResult("Test 3: Invalid course and name",
    register("Aman","C999").equals("Course C999 not found.")
     && !unknownName("").isEmpty()));
   resetTestData();
   results.add(testResult("Test 4: Prerequisite completion",
    register("Priya","CS102").startsWith("Finish these prerequisites")
     && register("Priya","CS101").startsWith("Registration successful")
     && complete("Priya","CS101").contains("completed")
     && register("Priya","CS102").startsWith("Registration successful")));
   return results;
  }finally{
   title=oldTitle;names=oldNames;capacity=oldCapacity;
   enrolled=oldEnrolled;waiting=oldWaiting;prereq=oldPrereq;done=oldDone;
  }
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

 static String testResult(String label,boolean passed){
  return (passed?"PASS  ":"FAIL  ")+label;
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

 static class RegistrationFrame extends JFrame{
  static final Color NAVY=new Color(23,39,65),BLUE=new Color(47,104,210);
  static final Color BACKGROUND=new Color(244,247,251),MUTED=new Color(102,116,139);
  DefaultTableModel tableModel;
  JTable table;
  JTextArea activity;
  JLabel courseValue,enrolledValue,waitingValue;

  RegistrationFrame(){
   super("Course Registration System");
   setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
   setMinimumSize(new Dimension(900,620));
   setSize(1100,740);
   setLocationRelativeTo(null);
   getContentPane().setBackground(BACKGROUND);
   setLayout(new BorderLayout());
   add(header(),BorderLayout.NORTH);
   add(sidebar(),BorderLayout.WEST);
   add(dashboard(),BorderLayout.CENTER);
   addWindowListener(new WindowAdapter(){
    @Override public void windowClosing(WindowEvent e){closeAndSave();}
   });
   refresh();
  }

  JPanel header(){
   JPanel p=new JPanel(new BorderLayout(12,0));
   p.setBackground(NAVY);
   p.setBorder(BorderFactory.createEmptyBorder(18,24,18,24));
   JLabel titleLabel=new JLabel("Course Registration");
   titleLabel.setForeground(Color.WHITE);
   titleLabel.setFont(new Font("SansSerif",Font.BOLD,22));
   JLabel sub=new JLabel("Manage enrollment, prerequisites and waiting lists");
   sub.setForeground(new Color(194,207,228));
   JPanel titles=new JPanel(new BorderLayout(0,4));
   titles.setOpaque(false);titles.add(titleLabel,BorderLayout.NORTH);
   titles.add(sub,BorderLayout.SOUTH);
   JButton saveButton=button("Save data",BLUE);
   saveButton.addActionListener(e->saveFromUi());
   p.add(titles,BorderLayout.WEST);p.add(saveButton,BorderLayout.EAST);
   return p;
  }

  JPanel sidebar(){
   JPanel p=new JPanel(new GridLayout(0,1,0,9));
   p.setBackground(Color.WHITE);
   p.setBorder(BorderFactory.createEmptyBorder(20,14,20,14));
   p.setPreferredSize(new Dimension(210,0));
   p.add(sideButton("Dashboard",()->refresh()));
   p.add(sideButton("Register student",()->edit("Register")));
   p.add(sideButton("Complete course",()->edit("Complete")));
   p.add(sideButton("Drop / leave queue",()->edit("Drop")));
   p.add(sideButton("Find student",()->findStudent()));
   p.add(sideButton("Run test cases",()->showTests()));
   JLabel note=new JLabel("<html><div style='padding:8px'>Changes are saved to<br>registrations.txt.</div></html>");
   note.setForeground(MUTED);
   p.add(note);
   return p;
  }

  JPanel dashboard(){
   JPanel p=new JPanel(new BorderLayout(14,14));
   p.setBackground(BACKGROUND);
   p.setBorder(BorderFactory.createEmptyBorder(22,22,18,22));
   JPanel top=new JPanel(new BorderLayout(0,14));
   top.setOpaque(false);
   JLabel heading=new JLabel("Overview");
   heading.setFont(new Font("SansSerif",Font.BOLD,23));
   heading.setForeground(NAVY);
   top.add(heading,BorderLayout.NORTH);
   JPanel cards=new JPanel(new GridLayout(1,3,12,0));
   cards.setOpaque(false);
   courseValue=new JLabel("0");enrolledValue=new JLabel("0");waitingValue=new JLabel("0");
   cards.add(statCard("COURSES",courseValue));
   cards.add(statCard("ENROLLED",enrolledValue));
   cards.add(statCard("WAITING",waitingValue));
   top.add(cards,BorderLayout.CENTER);
   p.add(top,BorderLayout.NORTH);

   tableModel=new DefaultTableModel(new Object[]{"Course","Title","Seats","Enrolled","Waiting"},0){
    @Override public boolean isCellEditable(int row,int column){return false;}
   };
   table=new JTable(tableModel);
   table.setRowHeight(30);
   table.setFont(new Font("SansSerif",Font.PLAIN,13));
   table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));
   table.getTableHeader().setBackground(new Color(231,237,246));
   table.setSelectionBackground(new Color(222,234,252));
   JScrollPane tableScroll=new JScrollPane(table);
   tableScroll.setBorder(BorderFactory.createTitledBorder("Course capacity and registrations"));
   p.add(tableScroll,BorderLayout.CENTER);

   activity=new JTextArea(4,20);
   activity.setEditable(false);
   activity.setLineWrap(true);
   activity.setWrapStyleWord(true);
   activity.setFont(new Font("SansSerif",Font.PLAIN,12));
   activity.setText("Ready. Choose an action from the left menu.");
   JScrollPane activityScroll=new JScrollPane(activity);
   activityScroll.setBorder(BorderFactory.createTitledBorder("Activity"));
   p.add(activityScroll,BorderLayout.SOUTH);
   return p;
  }

  JPanel statCard(String title,JLabel value){
   JPanel card=new JPanel(new BorderLayout(0,7));
   card.setBackground(Color.WHITE);
   card.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(226,232,241)),
    BorderFactory.createEmptyBorder(12,16,12,16)));
   JLabel label=new JLabel(title);
   label.setForeground(MUTED);label.setFont(new Font("SansSerif",Font.BOLD,11));
   value.setForeground(NAVY);value.setFont(new Font("SansSerif",Font.BOLD,24));
   card.add(label,BorderLayout.NORTH);card.add(value,BorderLayout.CENTER);
   return card;
  }

  JButton button(String text,Color color){
   JButton b=new JButton(text);
   b.setForeground(Color.WHITE);b.setBackground(color);
   b.setFocusPainted(false);b.setFont(new Font("SansSerif",Font.BOLD,12));
   b.setBorder(BorderFactory.createEmptyBorder(9,14,9,14));
   return b;
  }

  JButton sideButton(String text,Runnable action){
   JButton b=new JButton(text);
   b.setHorizontalAlignment(SwingConstants.LEFT);
   b.setForeground(NAVY);b.setBackground(new Color(246,248,252));
   b.setFocusPainted(false);b.setBorder(BorderFactory.createEmptyBorder(11,12,11,8));
   b.addActionListener(e->action.run());
   return b;
  }

  void refresh(){
   if(tableModel==null)return;
   tableModel.setRowCount(0);
   int totalEnrolled=0,totalWaiting=0;
   for(String id:new TreeSet<>(capacity.keySet())){
    List<String> roster=enrolled.get(id);
    List<String> queue=new ArrayList<>(waiting.get(id));
    totalEnrolled+=roster.size();totalWaiting+=queue.size();
    tableModel.addRow(new Object[]{id,title.get(id),
     seats(id)+" / "+capacity.get(id),join(roster),join(queue)});
   }
   courseValue.setText(String.valueOf(capacity.size()));
   enrolledValue.setText(String.valueOf(totalEnrolled));
   waitingValue.setText(String.valueOf(totalWaiting));
  }

  void edit(String action){
   JTextField studentField=new JTextField(16);
   JComboBox<String> courseBox=new JComboBox<>(new TreeSet<>(capacity.keySet()).toArray(new String[0]));
   JPanel form=new JPanel(new GridLayout(2,2,8,10));
   form.add(new JLabel("Student name"));form.add(studentField);
   form.add(new JLabel("Course"));form.add(courseBox);
   int choice=JOptionPane.showConfirmDialog(this,form,action+" course",
    JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
   if(choice!=JOptionPane.OK_OPTION)return;
   String message;
   try{
    String student=studentField.getText(),course=(String)courseBox.getSelectedItem();
    if(action.equals("Register"))message=register(student,course);
    else if(action.equals("Complete"))message=complete(student,course);
    else message=drop(student,course);
    save();refresh();log(message);
    JOptionPane.showMessageDialog(this,message,action,
     message.startsWith("Registration successful")||message.contains("got the seat")
      ?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE);
   }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),
    "Could not "+action.toLowerCase(),JOptionPane.ERROR_MESSAGE);}
  }

  void findStudent(){
   String student=JOptionPane.showInputDialog(this,"Student name:","Find student",
    JOptionPane.QUESTION_MESSAGE);
   if(student==null)return;
   try{
    student=name(student);
    List<String> found=new ArrayList<>();
    for(String id:new TreeSet<>(capacity.keySet())){
     if(isEnrolled(id,student))found.add(id+"  |  Enrolled");
     else if(isWaiting(id,student))found.add(id+"  |  Waiting");
    }
    String result=found.isEmpty()?"No registrations for "+student+".":String.join("\n",found);
    log("Student lookup: "+student);
    JOptionPane.showMessageDialog(this,result,"Student registrations",
     JOptionPane.INFORMATION_MESSAGE);
   }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),
    "Invalid student",JOptionPane.ERROR_MESSAGE);}
  }

  void showTests(){
   List<String> results=testCaseResults();
   String report=String.join("\n",results);
   log("Built-in tests completed.");
   JOptionPane.showMessageDialog(this,report,"Built-in test results",
    JOptionPane.INFORMATION_MESSAGE);
  }

  void saveFromUi(){
   try{save();log("Data saved to registrations.txt.");
    JOptionPane.showMessageDialog(this,"Registration data saved.","Saved",
     JOptionPane.INFORMATION_MESSAGE);
   }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),
    "Save failed",JOptionPane.ERROR_MESSAGE);}
  }

  void closeAndSave(){
   try{save();dispose();}
   catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),
    "Save failed",JOptionPane.ERROR_MESSAGE);}
  }

  void log(String message){
   activity.setText(message+"\n"+activity.getText());
  }
 }
}
