/*******************************************************************************
 * Copyright 2012, 2013, 2014 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Pēteris Paikens
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.semti.morphology.webservice;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class DictionaryResource extends ServerResource {
	@Get("xml")
	public String retrieve() {  
		String word = (String) getRequest().getAttributes().get("word");
		try {
			word = URLDecoder.decode(word, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String XML = "<explanation>";

		Utils.allowCORS(this);

		try {
			word = word.toLowerCase().trim();

			if (word.matches("[A-Za-zĀČĒĢĪĶĻŅŌŖŠŪŽāčēģīķļņōŗšūž0-9]+[-.]?")) {
				Properties access = new Properties();
				String conf = "dist/db.conf";
				access.load(new InputStreamReader(new FileInputStream(conf), "UTF-8"));

				Properties params = new Properties();
				params.put("user", access.getProperty("user"));
				params.put("password", access.getProperty("password"));
				params.put("charSet", "utf8");

				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection con = DriverManager.getConnection(access.getProperty("url"), params);

				PreparedStatement selEntries = con.prepareStatement("SELECT id, content FROM entries WHERE LOWER(word)=? ORDER BY word DESC");
				PreparedStatement selRedirect = con.prepareStatement("SELECT id, `to` FROM redirect WHERE `from`=?");
				PreparedStatement updEntries = con.prepareStatement("UPDATE entries SET hits=hits+1 WHERE id=?");
				PreparedStatement updRedirect = con.prepareStatement("UPDATE redirect SET hits=hits+1 WHERE id=?");
				PreparedStatement selMissing = con.prepareStatement("SELECT id FROM missing WHERE word=?");
				PreparedStatement insMissing = con.prepareStatement("INSERT INTO missing (word) VALUES (?)");
				PreparedStatement updMissing = con.prepareStatement("UPDATE missing SET hits=hits+1 WHERE id=?");
				PreparedStatement insLog = con.prepareStatement("INSERT INTO log (ip,`date`,word) VALUES (?,?,?)");
				PreparedStatement selAccess = con.prepareStatement("SELECT ip FROM access WHERE ip=?");

				// IP autorizācija
				String ip = getRequest().getClientInfo().getAddress();
				boolean valid_ip = false;
				selAccess.setString(1, ip);
				ResultSet rsAccess = selAccess.executeQuery();
				if (rsAccess.first()) {
					valid_ip = true;
				}
				
				if (valid_ip) {
					selEntries.setBytes(1, word.getBytes("UTF-8"));
					ResultSet rsEntries = selEntries.executeQuery();
					boolean success = false;

					if (rsEntries.isBeforeFirst()) {
						XML += "<dict id=\"SV\">";
					
						while (rsEntries.next()) {
							// 'ORDER BY word DESC' nodrošina, ka sugasvārdi tiek piedāvāti pirms īpašvārdiem
							XML += new String(rsEntries.getBytes("content"), "UTF-8");
							
							// Statistikai
							updEntries.setInt(1, rsEntries.getInt("id"));
							updEntries.executeUpdate();
							
							success = true;
						}
					}
					
					// Ja tieša atbilsme nav atrasta, meklējam "Sk." šķirkļos
					if (!success) {
						selRedirect.setBytes(1, word.getBytes("UTF-8"));
						ResultSet rsRedirect = selRedirect.executeQuery();
						
						if (rsRedirect.isBeforeFirst()) {
							XML += "<dict id=\"SV\" redirected_from=\"" + word + "\">";
							
							while (rsRedirect.next()) {
								// Pēc atrastajiem "Sk." šķirkļiem meklējam no jauna "parastajos" šķirkļos
								selEntries.setBytes(1, rsRedirect.getBytes("to"));
								rsEntries = selEntries.executeQuery();
								
								// Statistikai
								updRedirect.setInt(1, rsRedirect.getInt("id"));
								updRedirect.executeUpdate();
							
								while (rsEntries.next()) {
									XML += new String(rsEntries.getBytes("content"), "UTF-8");
									
									// Statistikai
									updEntries.setInt(1, rsEntries.getInt("id"));
									updEntries.executeUpdate();
									
									success = true;
								}
							}
						}
					}
					
					if (success) {
						XML += "</dict>";
					} else {
						selMissing.setBytes(1, word.getBytes("UTF-8"));
						ResultSet rsMissing = selMissing.executeQuery();

						if (!rsMissing.next()) {
							insMissing.setBytes(1, word.getBytes("UTF-8"));
							insMissing.executeUpdate();
						} else {
							updMissing.setInt(1, rsMissing.getInt("id"));
							updMissing.executeUpdate();
						}
					}
								
					insLog.setString(1, ip);
					insLog.setDate(2, new java.sql.Date((new java.util.Date()).getTime()));
					insLog.setBytes(3, word.getBytes("UTF-8"));
					insLog.executeUpdate();
				} else {
					XML += "Your IP address is not allowed to access this service.";
				}

				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		XML += "</explanation>";
		
		return XML;
	}
}
