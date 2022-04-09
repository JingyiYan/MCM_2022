import java.io.*;
import java.util.Date;
import java.util.*;
import java.text.*;
import java.lang.Math;
import static java.lang.Double.parseDouble;
import java.lang.Math.*;

class Main {
  public static double transB = 0.01;
  public static double transG = 0.01;
  public static BGC account;
  /**
   * This method only gives a basic decision on whether selling or buying
   * @return: int[0]:for operation of bitcoin: 0 for no operation, 1 for buy, 2 for sell
   *          int[1]:for operation of gold: 0 for no operation, 1 for buy, 2 for sell
  **/
  public static double[][] decision(Date d, Hashtable<Date,Double> tb, Hashtable<Date,Double> tg) {
    final long one_day = 24 * 3600000;
    //return int
    int[] rtn = {0,0};
    double[][] final_decision = {{0,0,0},{0,0,0}};
    //find previous 10 days data
    Date[] bdays = new Date[10];
    Date[] gdays = new Date[10];
    bdays[9] = d;
    gdays[9] = d;

    // find min and max
    double bmin = tb.get(d);
    double bmax = tb.get(d);

    //while loop for bitcoin ten days model
    int i = 8;
    while(i >= 0){
         bdays[i] = new Date(d.getTime() - one_day*(9-i));
         if(tb.get(bdays[i]) < bmin){
          bmin = tb.get(bdays[i]);
         }
         if(tb.get(bdays[i]) > bmax){
           bmax = tb.get(bdays[i]);
        }
       i--;
    }

    // compute mad
    double madb, madg;
    double diff_sumb = 0, diff_sumg = 0;
    double tol = 0;
    for (Date day: bdays) {
       tol += tb.get(day);
    }
    double avg = tol/10;
    for (Date day: bdays){
      diff_sumb += Math.abs(avg - tb.get(day));
    }
    madb = diff_sumb / 10;

    
    if(tb.get(d) > bmax-madb){
      rtn[0] = 2;
    } else if(tb.get(d) <= bmin+madb){
      rtn[0] = 1;
    } else{
      rtn[0] = 0;
    }
    
    if (!tg.containsKey(d)) {
      rtn[1] = 0;
    } else {
      double gmin = tg.get(d);
      double gmax = tg.get(d);
      //while loop for gold ten days model
      i = 8;
      int a = 1;
      while(i >= 0){
         if (tg.containsKey(new Date(d.getTime() - (one_day*a)))){
           gdays[i] = new Date(d.getTime() - one_day*a);
           if(tg.get(gdays[i]) < gmin){
            gmin = tg.get(gdays[i]);
           }
           if(tg.get(gdays[i]) > gmax){
             gmax = tg.get(gdays[i]);
           }
           i--;
         }
        a++;
      }
      double tolg = 0;
      for (Date day: gdays) {
       tolg += tg.get(day);
      }
      double avgg = tolg/10;
      
      for (Date day: gdays){
        diff_sumg += Math.abs(avgg - tg.get(day));
      }
      madg = diff_sumg / 10;
      if(tg.get(d) > gmax-madg){
        rtn[1] = 2;
      } else if(tg.get(d) <= gmin+madg){
        rtn[1] = 1;
      } else{
        rtn[1] = 0;
      }
    }
    
    if (rtn[0] == 2){
      if ((tb.get(d)*transB +account.getAvgBc())-(tb.get(d)) >= 0){
        rtn[0] = 0;
      }
    }
    if (rtn[1] == 2){
      if ((tg.get(d)*transG +account.getAvgGold())-(tg.get(d)) >= 0){
        rtn[1] = 0;
      }
    }
    System.out.println(rtn[0] + " " + rtn[1]);
    //compute precise model
    final_decision[0][0] = rtn[0];
    final_decision[1][0] = rtn[1];
    //buy
    if (rtn[0] == 1) {
      double fac1 = times(bdays, tb, false);
      double fac2 = distribution(false, 'b');
      double fac3 = buyingFac(d, tb, 'b');
      double avg_fac = (Math.pow((fac1*fac2*fac3), (1.0/3))) /100;

      System.out.println("f1 = " + fac1 + ", f2 = " + fac2 + ", f3 = " + fac3 + "avg = " + avg_fac);

      double act_pct = avg_fac*(account.MAX_BC - account.pct_bc);
      final_decision[0][1] = account.bgc[0]*act_pct;
      final_decision[0][2] = tb.get(d);
      if (account.bgc[0] == 0) {
        final_decision[0][1] = account.bgc[2]*(50.0/100)/tb.get(d);
        System.out.println(final_decision[0][1]);
      }
    }
    //sell
    if (rtn[0] == 2) {
      double fac1 = times(bdays, tb, true);
      double fac2 = distribution(true, 'b');
      double fac3 = maxSellingPrecentage(d,tb,'b');
      double avg_fac = Math.pow(fac1*fac2*fac3, 1.0/3)/100;
      double act_pct = avg_fac*(account.pct_bc - account.MIN_BC);
      final_decision[0][1] = account.bgc[0]*act_pct;
      final_decision[0][2] = tb.get(d);
    }
    //buy
    if (rtn[1] == 1) {
      double fac1 = times(gdays, tg, false);
      double fac2 = distribution(false, 'g');
      double fac3 = buyingFac(d, tg, 'g');
      double avg_fac = Math.pow(fac1*fac2*fac3, 1.0/3)/100;
      double act_pct = avg_fac*(account.MAX_GOLD - account.pct_gold);
      final_decision[1][1] = account.bgc[0]*act_pct;
      final_decision[1][2] = tg.get(d);
      if (account.bgc[1] == 0) {
        final_decision[1][1] = account.bgc[2]*(35.0/100)/tg.get(d);
      }
    }
    //sell
    if (rtn[1] == 2) {
      if(account.pct_gold <= account.MIN_GOLD) {
        final_decision[1][0] = 0;
        final_decision[1][1] = 0;
        final_decision[1][2] = 0;
      } else {
        double fac1 = times(gdays, tg, true);
        double fac2 = distribution(true, 'g');
        double fac3 = maxSellingPrecentage(d,tg,'g');
        double avg_fac = Math.pow(fac1*fac2*fac3, 1.0/3)/100;
        double act_pct = avg_fac*(account.pct_gold - account.MIN_GOLD);
        final_decision[1][1] = account.bgc[1]*act_pct;
        final_decision[1][2] = tg.get(d);
      }
      
    }
    return final_decision;
  }
  
  /**
   *Compute the relations between the times of increment/decrement and the units we should buy
    *@param Date[]: 10 days; Hashtable<Date,Double>: data; boolean: true for increament(selling), flase for decrement(buying)
    *@return double: percentage
  **/
  public static double times(Date[] days, Hashtable<Date,Double> table, boolean b) {
    double rtn = 0; //return percentage (0-100)
    int t = 0;
    Date prev = days[0];
    if (b) {
      for (Date d: days) {
        if (table.get(d) >= table.get(prev)) {
          t += 1;
        }
        else {
          t = 0;
        }
        prev = d;
      }
    }
    else {
      for (Date d: days) {
        if (table.get(d) <= table.get(prev)) {
          t += 1;
        }
        else {
          t = 0;
        }
        prev = d;
      }
    }
    rtn = Math.pow(t, 2.20959);
    return rtn;
  }
  /**
   * This method considers the current distribution of assests
   * @param: boolean: true for selling, false for buying 
   *         char: b for bitcoin, g for gold
   * @return: double: 0-100 percentage
  **/
  public static double distribution(boolean b, char c) {
    double rtn = 0;
    double ptc = 0;
    double max = 0;
    double min = 0;
    if (c == 'b') {
      ptc = account.pct_bc;
      max = account.MAX_BC;
      min = account.MIN_BC;
    } else {
      ptc = account.pct_gold;
      max = account.MAX_GOLD;
      min = account.MIN_GOLD;
    }
    System.out.println("pct :"+ ptc);
    System.out.println("max:" + max + " min:" + min + "bool:" + b);
    if (b) {
      if (ptc < min) {
        rtn = 0;
      } else {
        rtn = (ptc-min)/(max-min);
      }
    }
    else {
      if (ptc < min) {
        rtn = (max-ptc)/max;
      } else{
      rtn = (max-ptc)/(max-min);
      }
    }
    System.out.println("rtn = " + rtn);
    //System.out.println("distribution factor: " + (Math.log(rtn)+1)*100);
    double rtn_fin = Math.log(rtn-(1.0/(1-Math.exp(1))))-Math.log(-1.0/(1.0-Math.exp(1)));
    System.out.println(rtn_fin);
    return rtn_fin*100;
  }
  

  /**
   * factor 3: compare with history transaction with max profit
  **/
  public static double maxSellingPrecentage(Date d, Hashtable<Date, Double> t, char c){
    if(c == 'b'){
      double currPBC = (t.get(d) - account.getAvgBc()) / account.avg_bc * 100;
      if(currPBC > account.getHisBC()){
        account.setHisBC(currPBC);
        return 100;
      } else{
        double a=2*Math.log(10)/Math.log(account.getHisBC());
        return Math.pow(currPBC,a)*100;
      }
    } else {
      double currPGD = (t.get(d) - account.getAvgGold()) / account.avg_gold * 100;
      if (currPGD > account.getHisGold()){
        account.setHisGold(currPGD);
        return 100;
      } else {
        double a=2*Math.log(10)/Math.log(account.getHisGold());
        return Math.pow(currPGD,a)*100;
      }
    }
  }

  /**
    * buying factor
  **/
  public static double buyingFac(Date d, Hashtable<Date, Double> t, char c) {
    double rtn = 0;
    if (c == 'b') {
      if (account.bgc[0] <= account.MIN_BC) {
        return 100;
      }
      else if (account.bgc[0] <= 50) {
        return 50;
      } else {
        return 25;
      }
    } else if (c == 'g'){
      if (account.bgc[1] <= account.MIN_GOLD) {
        return 100;
      }
      else if (account.bgc[1] < account.MAX_GOLD) {
        return 50;
      } else {
        return 0;
      }
    }
    return rtn;
  }

    
  public static void makeOpB(double[] d){
    if(d[0] ==0){
      System.out.println("Bitcoin: Do Nothing.");
    } else if (d[0] == 1){   // buy
      account.buyB(d[1], d[2]);
      System.out.println("Bitcoin: Decision: buy " + d[1] +" unit. Price at:" + d[2]);
    } else {            // sell
      account.sellB(d[1],d[2]);
      System.out.println ("Bitcoin: Decision: sell " + + d[1] +" unit. Price at:" + d[2]);
    }
  }

  public static void makeOpG(double[] d){
    if(d[0] ==0){
      System.out.println("Gold: Do Nothing.");
    } else if (d[0] == 1){   // buy
      account.buyG(d[1], d[2]);
      System.out.println("Gold: Decision: buy " + d[1] +" unit. Price at: " + d[2]);
    } else {            // sell
      account.sellG(d[1], d[2]);
      System.out.println ("Gold: Decision: sell " + + d[1] +"unit. Price at: " + d[2]);
    }
  }


  /**
   * Read data into hastable
   * @return hastable
  **/
  public static Hashtable<Date, Double> readFile(String fname) throws IOException, ParseException{
    FileInputStream fstream = new FileInputStream(fname);
    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
    br.readLine(); // skip the first line(s)
    String line;
    Hashtable<Date, Double> rtn_dic = new Hashtable<Date, Double>();
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    while ((line = br.readLine())!=null) {
      String[] token = line.split(",");
      Date date = sdf.parse(token[0]);
      double val;
      if (token.length == 1){
        val = 0;
      } else {
        val = parseDouble(token[1]);
      }
      rtn_dic.put(date, val);
    }
    fstream.close();

    return rtn_dic;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException, ParseException{
    //set BGC account
    account = new BGC();

    //read file data into hastables
    String bcFile = "BCHAIN-MKPRU.csv";
    String goldFile = "LBMA-GOLD.csv";
    Hashtable<Date, Double> bc_dic = readFile(bcFile);
    Hashtable<Date, Double> gold_dic = readFile(goldFile);
    
    // // Enter a date in format M/DD/YY
    // Scanner in = new Scanner(System.in);  // Create a Scanner object
    // System.out.print("Enter date(MM/DD/YY): ");
    // String str_date = in.nextLine();
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    // Date d_date = sdf.parse(str_date);   // this is the current date
    // System.out.println(d_date.toString());
    // in.close();

    Date date = sdf.parse("9/23/16");
    Date end = sdf.parse("12/23/17");
    while(date.compareTo(end) < 0) {
      System.out.println("------------- Decision " + date + " ------------");
      // Decision making process
      double[][] d = decision(date, bc_dic, gold_dic);
      double[] db = d[0];
      double[] dg = d[1];
      
      // bitcoin
      makeOpB(db);
      // gold
      if (gold_dic.containsKey(date)){
        makeOpG(dg);
      }
      date = new Date(date.getTime() + 24 * 3600000);
    }
  
  }
}