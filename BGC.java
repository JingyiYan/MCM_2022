public class BGC{
  // transanction commission constants
  public final double fee_gold = 0.01;
  public final double fee_bc = 0.01;
  
  // max & min percentage for each
  // TODO: modify later
  final double MAX_BC = 0.5 + 0.08825;
  final double MIN_BC = 0.5 - 0.08825;
  final double MAX_CASH = 0.30;
  final double MIN_CASH = 0;
  final double MAX_GOLD = 0.35 + 0.06175;
  final double MIN_GOLD = 0.35 - 0.06175;

  double bgc[];
  double avg_gold;  // reference constants for decision making
  double avg_bc;

  double pct_bc;
  double pct_cash;
  double pct_gold;

  double his_max_b, his_max_g;  // historical maximum unit profit measured in percentage

  double total;  // total value
  
  public BGC() {
    bgc = new double[]{0,0,1000};
    total = 1000;
    avg_bc = 0;
    avg_gold = 0;
    his_max_b = 0;
    his_max_g = 0;
    pct_bc = 0;
    pct_cash = 0;
    pct_gold = 0;
  }
  
  public void buyB(double unit, double mp) {
    avg_bc = (avg_bc*bgc[0] + unit*mp)/(bgc[0]+unit);  
    bgc[0] += unit;
    bgc[2] -= (unit*mp + unit*mp*fee_bc);
    updatePercentage(mp);
  }

  public void buyG(double unit, double mp) {
    avg_gold = (avg_bc*bgc[1] + unit*mp)/(bgc[1]+unit);
    bgc[1] += unit;
    bgc[2] -= (unit*mp + unit*mp*fee_gold);
    updatePercentage(mp);
  }

  public void sellB(double unit, double mp) {
    bgc[0] -= unit;
    bgc[2] += unit*mp - unit*mp*fee_bc;
    updatePercentage(mp);
  }

  public void sellG(double unit, double mp) {
    bgc[1] -= unit;
    bgc[2] += unit*mp - unit*mp*fee_gold;
    updatePercentage(mp);
  }

  public void updatePercentage(double mp){
    total = bgc[0]*mp + bgc[1]*mp + bgc[2];
    pct_bc = bgc[0]*mp/total;
    pct_cash = bgc[2]/total;
    pct_gold = bgc[1]*mp/total;
    System.out.println("account = " + bgc[0] + " " + bgc[1] + " " + bgc[2]);
    System.out.println("total = " + total + "pct " + pct_bc + " " + pct_gold);
  }

  public double getAvgGold() {
    return avg_gold;
  }

  public double getAvgBc() {
    return avg_bc;
  }

  public double getHisBC() {
    return his_max_b;
  }
  public double getHisGold(){
    return his_max_g;
  }

  public void setHisBC(double new_b){
    this.his_max_b = new_b;
  }
  public void setHisGold(double new_g){
    this.his_max_g = new_g;
  }
}