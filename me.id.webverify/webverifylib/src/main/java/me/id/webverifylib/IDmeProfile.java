package me.id.webverifylib;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public final class IDmeProfile implements Parcelable {
  private String id;
  private boolean verified;
  private String affiliation;
  private String fname;
  private String lname;
  private String zip;
  private String email;
  private String uuid;
  private String group;

  IDmeProfile(@NonNull String json) throws JSONException {
    JSONObject jsonProfile = new JSONObject(json);
    id = getJsonField(jsonProfile, "id");
    verified = jsonProfile.getBoolean("verified");
    affiliation = getJsonField(jsonProfile, "affiliation");
    fname = getJsonField(jsonProfile, "fname");
    lname = getJsonField(jsonProfile, "lname");
    zip = getJsonField(jsonProfile, "zip");
    email = getJsonField(jsonProfile, "email");
    uuid = getJsonField(jsonProfile, "uuid");
    group = getJsonField(jsonProfile, "group");
  }

  private String getJsonField(JSONObject jsonProfile, String id) {
    String field = null;
    try {
      field = jsonProfile.getString(id);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return field == null || field.equals("null") ? null : field;
  }

  public String getId() {
    return id;
  }

  public boolean isVerified() {
    return verified;
  }

  public String getAffiliation() {
    return affiliation;
  }

  public String getFname() {
    return fname;
  }

  public String getLname() {
    return lname;
  }

  public String getZip() {
    return zip;
  }

  public String getEmail() {
    return email;
  }

  public String getUuid() {
    return uuid;
  }

  public String getGroup() {
    return group;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeByte(this.verified ? (byte) 1 : (byte) 0);
    dest.writeString(this.affiliation);
    dest.writeString(this.fname);
    dest.writeString(this.lname);
    dest.writeString(this.zip);
    dest.writeString(this.email);
    dest.writeString(this.uuid);
    dest.writeString(this.group);
  }

  protected IDmeProfile(Parcel in) {
    this.id = in.readString();
    this.verified = in.readByte() != 0;
    this.affiliation = in.readString();
    this.fname = in.readString();
    this.lname = in.readString();
    this.zip = in.readString();
    this.email = in.readString();
    this.uuid = in.readString();
    this.group = in.readString();
  }

  public static final Parcelable.Creator<IDmeProfile> CREATOR = new Parcelable.Creator<IDmeProfile>() {
    @Override
    public IDmeProfile createFromParcel(Parcel source) {
      return new IDmeProfile(source);
    }

    @Override
    public IDmeProfile[] newArray(int size) {
      return new IDmeProfile[size];
    }
  };

  @Override
  public String toString() {
    return String.format(Locale.getDefault(),
        "id = %s verified = %s affiliation = %s fname = %s lname = %s zip = %s email = %s uuid = %s group = %s",
        id,
        verified,
        affiliation,
        fname,
        lname,
        zip,
        email,
        uuid,
        group);
  }
}
