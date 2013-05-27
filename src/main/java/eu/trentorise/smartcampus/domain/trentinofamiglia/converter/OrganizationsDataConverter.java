package eu.trentorise.smartcampus.domain.trentinofamiglia.converter;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.protobuf.ByteString;

import eu.trentorise.smartcampus.domain.discovertrento.GenericPOI;
import eu.trentorise.smartcampus.domain.discovertrento.POIData;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.EventoFamiglia;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.OrganizzazioneFamiglia;

public class OrganizationsDataConverter implements DataConverter {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private static final String TYPE_FAMILY = "Family";   
	
//	private static final int DURATION = (24*60 - 1)*60*1000;
	
	@Override
	public Serializable toMessage(Map<String, Object> parameters) {
		if (parameters == null)
			return null;
		return new HashMap<String, Object>(parameters);
	}
	
	@Override
	public Object fromMessage(Serializable object) {
		List<ByteString> data = (List<ByteString>) object;
		Tuple res = new Tuple();
		List<GenericPOI> list = new ArrayList<GenericPOI>();
		for (ByteString bs : data) {
			try {
				OrganizzazioneFamiglia org = OrganizzazioneFamiglia.parseFrom(bs);
				GenericPOI gp = extractGenericPOI(org);
				list.add(gp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(OrganizzazioneFamiglia org) throws ParseException {
		GenericPOI gp = new GenericPOI();
		
		POIData pd = new POIData(org.getPoi());
		
		gp.setPoiData(pd);
		gp.setType(TYPE_FAMILY + " - Organizations");
		
		gp.setSource("TrentinoFamiglia");
		
		gp.setTitle(org.getName());
		gp.setDescription(org.getLink());
		
		gp.setId(org.getPoi().getPoiId());
		
		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("status", org.getStatus());

		try {
			gp.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {}		
		
		return gp;
	}

	private String createDescription(EventoFamiglia ev) {
		StringBuilder descr = new StringBuilder();
		descr.append(ev.getDescription());
		if (ev.hasOrganization()) {
			descr.append("<br/>");
			descr.append("Organizzazione: ");
			descr.append(ev.getOrganization());
		}
		String s = descr.toString();
		s = s.replace("\n", " ");
		s = s.replace("\t", " ");
		return s;
	}

	private static String encode(String s) {
		return new BigInteger(s.getBytes()).toString(16);
	}
}
