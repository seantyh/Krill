package de.ids_mannheim.korap.query.spans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.search.spans.Spans;

/** A span kept as a candidate for matching with another Span
 * 	@author margaretha
 * */
public class CandidateSpan implements Comparable<CandidateSpan>{	
	private int doc,start,end;
	private long cost;
	private Collection<byte[]> payloads = new ArrayList<>();
	private int position;
	private CandidateSpan childSpan; // used for multiple distance with unordered constraint 
	private short elementRef;
	
	
	public CandidateSpan(Spans span) throws IOException {
		this.doc = span.doc();
		this.start = span.start();
		this.end = span.end();
		this.cost = span.cost();		
		if (span.isPayloadAvailable())
			setPayloads(span.getPayload());
		
		/*if (span instanceof ElementSpans ){
			ElementSpans s = (ElementSpans) span;
			this.elementRef = s.getElementRef();
		}
		else if (span instanceof AttributeSpans){
			AttributeSpans s = (AttributeSpans) span;
			this.elementRef = s.getElementRef();
		}		*/
	}	
	
	public CandidateSpan(Spans span, int position) throws IOException {
		this(span);
		this.position = position;		
	}
	
	public CandidateSpan(int start, int end, int doc, long cost,
			Collection<byte[]> payloads) {
		this.start = start;
		this.end = end;
		this.doc = doc;
		this.cost = cost;
		if (payloads != null) setPayloads(payloads);
	}

	public int getDoc() {
		return doc;
	}
	public void setDoc(int doc) {
		this.doc = doc;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	public Collection<byte[]> getPayloads() {
		return payloads;
	}

	public void setPayloads(Collection<byte[]> payloads) {
		for (byte[] b : payloads){			
			this.payloads.add(b.clone());
		}
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public CandidateSpan getChildSpan() {
		return childSpan;
	}

	public void setChildSpan(CandidateSpan childSpan) {
		this.childSpan = childSpan;
	}

	public short getElementRef() {
		return elementRef;
	}

	public void setElementRef(short elementRef) {
		this.elementRef = elementRef;
	}

	@Override
	public int compareTo(CandidateSpan o) {
		if (this.getStart() == o.getStart()){
			if (this.getEnd() == o.getEnd())
				return 0;	
			if (this.getEnd() > o.getEnd() )
				return 1;
			else return -1;
		}
		else if (this.getStart() < o.getStart())
			return -1;
		else 
			return 1;	
	}
}
