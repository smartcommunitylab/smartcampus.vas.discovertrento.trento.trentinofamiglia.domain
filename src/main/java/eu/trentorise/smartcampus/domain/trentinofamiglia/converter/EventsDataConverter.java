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

import eu.trentorise.smartcampus.domain.discovertrento.GenericEvent;
import eu.trentorise.smartcampus.service.trentinofamiglia.data.message.Trentinofamiglia.EventoFamiglia;

public class EventsDataConverter implements DataConverter {

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
		List<GenericEvent> list = new ArrayList<GenericEvent>();
		for (ByteString bs : data) {
			try {
				EventoFamiglia ev = EventoFamiglia.parseFrom(bs);
				GenericEvent ge = extractGenericEvent(ev);
				list.add(ge);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericEvent[list.size()]));
		return res;
	}

	private GenericEvent extractGenericEvent(EventoFamiglia ev) throws ParseException {
		GenericEvent ge = new GenericEvent();
		
		ge.setDescription(createDescription(ev));
		
		ge.setSource("TrentinoFamiglia");

		ge.setFromTime(sdf.parse(ev.getFrom()).getTime());
		ge.setToTime(sdf.parse(ev.getTo()).getTime());
		ge.setTiming(ev.getDays());
		
		ge.setType(TYPE_FAMILY);
		
		if (ev.hasPlace()) {
			ge.setAddressString(ev.getPlace());
		}
		
		String s = ev.getId();
		ge.setId(encode(s));
		if (ev.hasPoi()) {
			ge.setPoiId(ev.getPoi().getPoiId());
		}
		ge.setTitle(ev.getTitle());

		Map<String,Object> map = new TreeMap<String, Object>();
		map.put("certified", ev.getCertified());
		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ge;
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
