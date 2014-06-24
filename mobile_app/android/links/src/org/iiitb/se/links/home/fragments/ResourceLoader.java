package org.iiitb.se.links.home.fragments;

import org.scribe.model.Token;

public interface ResourceLoader {
  public void fetchProtectedResource(final Token accessToken);
  public void startAuthorize();
}
