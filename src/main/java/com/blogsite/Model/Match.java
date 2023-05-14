package com.blogsite.Model;
public class Match {
//  private String matchId;
  private String team1;
  private String team2;
  private String matchType;
  private String matchStatus;

  public Match(String team1, String team2, String matchType, String matchStatus) {
//    this.matchId = matchId;
      this.team1 = team1;
      this.team2 = team2;
      this.matchType = matchType;
      this.matchStatus = matchStatus;
  }

//  public String getMatchId() {
//      return matchId;
//  }

//  public void setMatchId(String matchId) {
//      this.matchId = matchId;
//  }

  public String getTeam1() {
      return team1;
  }

  public void setTeam1(String team1) {
      this.team1 = team1;
  }

  public String getTeam2() {
      return team2;
  }

  public void setTeam2(String team2) {
      this.team2 = team2;
  }

  public String getMatchType() {
      return matchType;
  }

  public void setMatchType(String matchType) {
      this.matchType = matchType;
  }

  public String getMatchStatus() {
      return matchStatus;
  }

  public void setMatchStatus(String matchStatus) {
      this.matchStatus = matchStatus;
  }
}
