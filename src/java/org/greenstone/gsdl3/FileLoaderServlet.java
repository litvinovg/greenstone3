package org.greenstone.gsdl3;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.greenstone.gsdl3.util.GSParams;
import org.greenstone.util.GlobalProperties;

public class FileLoaderServlet extends LibraryServlet
{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		if (request.getMethod().equals("POST"))
		{
			DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

			int sizeLimit = System.getProperties().containsKey("servlet.upload.filesize.limit") ? Integer.parseInt(System.getProperty("servlet.upload.filesize.limit")) : 20 * 1024 * 1024;

			fileItemFactory.setSizeThreshold(sizeLimit);
			fileItemFactory.setRepository(new File(GlobalProperties.getGSDL3Home() + File.separator + "tmp"));

			ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);

			String storageLocation = "";
			File uploadedFile = null;
			Boolean ajaxUpload = false;
			StringBuilder json = new StringBuilder("[");
			try
			{
				List items = uploadHandler.parseRequest(request);
				Iterator iter = items.iterator();
				while (iter.hasNext())
				{
					FileItem current = (FileItem) iter.next();
					if (current.isFormField())
					{
						if (current.getFieldName().equals(GSParams.FILE_LOCATION))
						{
							storageLocation = current.getString();
						}
						
						if (current.getFieldName().equals("ajaxUpload") && current.getString().equals("true"))
						{
							ajaxUpload = true;
						}
					}
					else
					{
						File file = new File(GlobalProperties.getGSDL3Home() + File.separator + "tmp" + File.separator + current.getName());
						File tmpFolder = new File(GlobalProperties.getGSDL3Home() + File.separator + "tmp");
						if (!tmpFolder.exists())
						{
							tmpFolder.mkdirs();
						}
						current.write(file);

						uploadedFile = file;
						
						if(!json.toString().equals("["))
						{
							json.append(",");
						}
						
						json.append("{");
						json.append("\"name\":\"" + file.getName() + "\",");
						json.append("\"size\":\"" + file.length() + "\",");
						json.append("\"url\":\"" + "tmp/" + current.getName() + "\",");
						json.append("\"thumbnail_url\":\"" + "tmp/" + current.getName() + "\",");
						json.append("\"delete_url\":\"\",");
						json.append("\"delete_type\":\"\"");
						json.append("}");
					}
				}
				json.append("]");
				
				if(ajaxUpload)
				{
					response.setContentType("application/json");
					PrintWriter writer = response.getWriter();
					writer.write(json.toString());
					writer.flush();
					
					return;
				}

				if (!storageLocation.equals("") && uploadedFile != null)
				{
					String[] locations = storageLocation.split(":");

					for (String location : locations)
					{
						File toFile = new File(GlobalProperties.getGSDL3Home() + location);
						if (toFile.exists())
						{
							File backupFile = new File(toFile.getAbsolutePath() + System.currentTimeMillis());

							logger.info("Backing up file (" + toFile.getAbsolutePath() + ") to " + backupFile.getAbsolutePath());
							toFile.renameTo(backupFile);
						}

						FileChannel source = null;
						FileChannel destination = null;
						try
						{
							logger.info("Moving uploaded file (" + uploadedFile.getAbsolutePath() + ") to " + toFile.getAbsolutePath());
							source = new FileInputStream(uploadedFile).getChannel();
							destination = new FileOutputStream(toFile).getChannel();
							destination.transferFrom(source, 0, source.size());
						}
						finally
						{
							if (source != null)
							{
								source.close();
							}
							if (destination != null)
							{
								destination.close();
							}
						}

					}
				}
			}
			catch (Exception e)
			{
				logger.error("Exception in LibraryServlet -> " + e.getMessage());
			}
		}
		else
		{
			Map<String, String[]> queryMap = request.getParameterMap();
			Iterator<String> queryIter = queryMap.keySet().iterator();
			
			while(queryIter.hasNext())
			{
				String q = queryIter.next();
				if (q.equals("downloadFile"))
				{
					String fileLocation = queryMap.get(q)[0];
					File fileToGet = new File(GlobalProperties.getGSDL3Home() + File.separator + fileLocation);
	
					if (fileToGet.exists())
					{
						response.setContentType("application/octet-stream");
						response.addHeader("Content-Disposition","attachment;filename=" + fileToGet.getName());
						FileInputStream fis = new FileInputStream(fileToGet);
						ServletOutputStream sos = response.getOutputStream();
	
						byte[] buffer = new byte[4096];
						int len;
						while ((len = fis.read(buffer)) != -1)
						{
							sos.write(buffer, 0, len);
						}
						sos.flush();
						fis.close();
						sos.close();
						
						return;
					}
				}
			}
		}

		super.doGet(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
