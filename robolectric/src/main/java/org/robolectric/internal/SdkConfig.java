package org.robolectric.internal;

import android.os.Build;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.util.Logger;

public class SdkConfig implements Comparable<SdkConfig> {

  private static final Map<Integer, SdkVersion> SUPPORTED_APIS = Collections.unmodifiableMap(new HashMap<Integer, SdkVersion>() {
    private final double jdkVersion = Double.parseDouble(System.getProperty("java.specification.version"));

    {
      addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "0", "1.6", "REL");
      addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "0", "1.6", "REL");
      addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "0", "1.6", "REL");
      addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "1", "1.7", "REL");
      addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.0_r2", "1", "1.7", "REL");
      addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "1", "1.7", "REL");
      addSdk(Build.VERSION_CODES.M, "6.0.1_r3", "0", "1.7", "REL");
      addSdk(Build.VERSION_CODES.N, "7.0.0_r1", "0", "1.8", "REL");
      addSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "0", "1.8", "REL");
      addSdk(Build.VERSION_CODES.O, "o-preview-4", "0", "1.8", "O");
    }

    private void addSdk(int sdkVersion, String androidVersion, String frameworkSdkBuildVersion, String minJdkVersion, String codeName) {
      if (jdkVersion >= Double.parseDouble(minJdkVersion)) {
        put(sdkVersion, new SdkVersion(androidVersion, frameworkSdkBuildVersion, codeName));
      } else {
        Logger.info("Android SDK %s not supported on JDK %s (it requires %s)", sdkVersion, jdkVersion, minJdkVersion);
      }
    }
  });

  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;
  public static final int MAX_SDK_VERSION = Collections.max(getSupportedApis());

  private final int apiLevel;

  public static Set<Integer> getSupportedApis() {
    return SUPPORTED_APIS.keySet();
  }

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public String getAndroidVersion() {
    return getSdkVersion().androidVersion;
  }

  public String getAndroidCodeName() {
    return getSdkVersion().codeName;
  }

  public DependencyJar getAndroidSdkDependency() {
    return createDependency("org.robolectric", "android-all", getSdkVersion().getAndroidVersion() + "-robolectric-" + getSdkVersion().getRobolectricVersion(), null);
  }

  @Override
  public boolean equals(Object that) {
    return that == this || that instanceof SdkConfig && ((SdkConfig) that).apiLevel == (apiLevel);
  }

  @Override
  public int hashCode() {
    return apiLevel;
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int compareTo(@Nonnull SdkConfig o) {
    return apiLevel - o.apiLevel;
  }

  private SdkVersion getSdkVersion() {
    final SdkVersion sdkVersion = SUPPORTED_APIS.get(apiLevel);
    if (sdkVersion == null) {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ".");
    }
    return sdkVersion;
  }

  private DependencyJar createDependency(String groupId, String artifactId, String version, String classifier) {
    return new DependencyJar(groupId, artifactId, version, classifier);
  }

  private static final class SdkVersion {
    private final String androidVersion;
    private final String robolectricVersion;
    private final String codeName;

    SdkVersion(String androidVersion, String robolectricVersion, String codeName) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
    }

    @Override
    public boolean equals(Object that) {
      return that == this || that instanceof SdkVersion && equals((SdkVersion) that);
    }

    public boolean equals(SdkVersion that) {
      return that == this ||
          Objects.equals(that.androidVersion, androidVersion) &&
              Objects.equals(that.robolectricVersion, robolectricVersion);
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    public String getAndroidVersion() {
      return androidVersion;
    }

    public String getRobolectricVersion() {
      return robolectricVersion;
    }
  }
}
