package com.mymap.mymap;

import com.mymap.mymap.domain.params.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
class busRouteFilter {

	HashSet<String> pobangDP = new HashSet<>(Arrays.asList("서대문11","서대문13","프리패스"));
	HashSet<String> ganhoDP = new HashSet<>(Arrays.asList("7018","7730","163","110A"));
	HashSet<String> hongjeTF = new HashSet<>(Arrays.asList("701","702A","702B","703","704","705","706","707","708","709","720","741","752","N37","7019","7021","7025","790","799","서대문11","9709","9710","9709N","9710-1","6005"));
	HashSet<String> harimgakTF = new HashSet<>(Arrays.asList("7018","1020","1711","7016","7022","7212","8002","종로13"));
	HashSet<String> seoulTF = new HashSet<>(Arrays.asList("100","150","151","152","162","202","421","500","501","502","503","504","505","506","507","742","750A","750B","752","N15","N75"));
	HashSet<String> seoul6TF = new HashSet<>(Arrays.asList("103","173","201","261","262","603","701","702A","702B","704","708","709","7017","7021","7022","7024","TOUR11","M4108","M4130","M4137","M5107","M5115","M5121","1102","4108","8800","9301","8800(예약)","P9110(출근)","P9110(퇴근)"));
	HashSet<String> wonhyoAR = new HashSet<>(Arrays.asList("162","262","400","503","0411용산","0411강남","1711","7016"));
	HashSet<String> shinyongsanAR = new HashSet<>(Arrays.asList("100","150","151","152","500","501","504","506","507","605","742","750A","750B","752","N15","6001","N6001","N6002"));
	HashSet<String> yongsan17AR = new HashSet<>(Arrays.asList("505","용산03","프리패스"));

	Map<String,Set<String>> routes = new ConcurrentHashMap<>();

	@Autowired
	private FilteredBusRepository filteredBusRepository;
	@Autowired
	private MarkerClusterRepository markerClusterRepository;
	@Autowired
	private ParamsService paramsService;

	@Test
	void way3() {
//		HashSet<String> pobangDP = new HashSet<>(Arrays.asList("서대문11","서대문13","프리패스"));
//		HashSet<String> ganhoDP = new HashSet<>(Arrays.asList("7018","7730","163","110A"));
//		HashSet<String> hongjeTF = new HashSet<>(Arrays.asList("701","702A","702B","703","704","705","706","707","708","709","720","741","752","N37","7019","7021","7025","790","799","서대문11","9709","9710","9709N","9710-1","6005"));
//		HashSet<String> harimgakTF = new HashSet<>(Arrays.asList("7018","1020","1711","7016","7022","7212","8002","종로13"));
//		HashSet<String> seoulTF = new HashSet<>(Arrays.asList("100","150","151","152","162","202","421","500","501","502","503","504","505","506","507","742","750A","750B","752","N15","N75"));
//		HashSet<String> seoul6TF = new HashSet<>(Arrays.asList("103","173","201","261","262","603","701","702A","702B","704","708","709","7017","7021","7022","7024","TOUR11","M4108","M4130","M4137","M5107","M5115","M5121","1102","4108","8800","9301","8800(예약)","P9110(출근)","P9110(퇴근)"));
//		HashSet<String> wonhyoAR = new HashSet<>(Arrays.asList("162","262","400","503","0411용산","0411강남","1711","7016"));
//		HashSet<String> shinyongsanAR = new HashSet<>(Arrays.asList("100","150","151","152","500","501","504","506","507","605","742","750A","750B","752","N15","6001","N6001","N6002"));
//		HashSet<String> yongsan17AR = new HashSet<>(Arrays.asList("505","용산03","프리패스"));

		HashSet<String> DPs = new HashSet<>();
		DPs.addAll(pobangDP);
		DPs.addAll(ganhoDP);
		HashSet<String> TFs = new HashSet<>();
		TFs.addAll(hongjeTF);
		TFs.addAll(harimgakTF);
		TFs.addAll(seoulTF);
		TFs.addAll(seoul6TF);
		HashSet<String> ARs = new HashSet<>();
		ARs.addAll(wonhyoAR);
		ARs.addAll(shinyongsanAR);
		ARs.addAll(yongsan17AR);

		System.out.println("==== 최초 상태 ====");
		System.out.println(DPs);
		System.out.println(ARs);
		DPs.retainAll(ARs);
		System.out.println("=== 프리패스 필터링 ===");
		System.out.println(DPs);

		pobangDP.retainAll(TFs);
		ganhoDP.retainAll(TFs);
		wonhyoAR.retainAll(TFs);
		shinyongsanAR.retainAll(TFs);
		yongsan17AR.retainAll(TFs);

		DPs.addAll(pobangDP);
		DPs.addAll(ganhoDP);
		ARs.clear();
		ARs.addAll(wonhyoAR);
		ARs.addAll(shinyongsanAR);
		ARs.addAll(yongsan17AR);
		System.out.println("=== 환승지 필터링 후=== ");
		System.out.println(DPs);
		System.out.println(ARs);

		HashSet<String> DPAR = new HashSet<>();
		DPAR.addAll(DPs);
		DPAR.addAll(ARs);

		hongjeTF.retainAll(DPAR);
		harimgakTF.retainAll(DPAR);
		seoulTF.retainAll(DPAR);
		seoul6TF.retainAll(DPAR);
		System.out.println("=== 최종 ===");
		System.out.println(pobangDP);
		System.out.println(ganhoDP);
		System.out.println(hongjeTF);
		System.out.println(harimgakTF);
		System.out.println(seoulTF);
		System.out.println(seoul6TF);
		System.out.println(wonhyoAR);
		System.out.println(yongsan17AR);
		System.out.println(shinyongsanAR);

	}

	@Test
	void way4() {
//		HashSet<String> pobangDP = new HashSet<>(Arrays.asList("서대문11","서대문13","프리패스"));
//		HashSet<String> ganhoDP = new HashSet<>(Arrays.asList("7018","7730","163","110A"));
//		HashSet<String> hongjeTF = new HashSet<>(Arrays.asList("701","702A","702B","703","704","705","706","707","708","709","720","741","752","N37","7019","7021","7025","790","799","서대문11","9709","9710","9709N","9710-1","6005"));
//		HashSet<String> harimgakTF = new HashSet<>(Arrays.asList("7018","1020","1711","7016","7022","7212","8002","종로13"));
//		HashSet<String> seoulTF = new HashSet<>(Arrays.asList("100","150","151","152","162","202","421","500","501","502","503","504","505","506","507","742","750A","750B","752","N15","N75"));
//		HashSet<String> seoul6TF = new HashSet<>(Arrays.asList("103","173","201","261","262","603","701","702A","702B","704","708","709","7017","7021","7022","7024","TOUR11","M4108","M4130","M4137","M5107","M5115","M5121","1102","4108","8800","9301","8800(예약)","P9110(출근)","P9110(퇴근)"));
//		HashSet<String> wonhyoAR = new HashSet<>(Arrays.asList("162","262","400","503","0411용산","0411강남","1711","7016"));
//		HashSet<String> shinyongsanAR = new HashSet<>(Arrays.asList("100","150","151","152","500","501","504","506","507","605","742","750A","750B","752","N15","6001","N6001","N6002"));
//		HashSet<String> yongsan17AR = new HashSet<>(Arrays.asList("505","용산03","프리패스"));

		//int hongjeDepth, harimgakDepth, seoulDepth, seoul6Depth = 0;

		Map<String,Set<String>> departureGroup = new ConcurrentHashMap<>();
		departureGroup.putIfAbsent("13550",pobangDP);
		departureGroup.putIfAbsent("13168",ganhoDP);
		Map<String,Set<String>> transferGroup = new ConcurrentHashMap<>();
		transferGroup.putIfAbsent("13028",hongjeTF);
		transferGroup.putIfAbsent("01136",harimgakTF);
		transferGroup.putIfAbsent("02004",seoulTF);
		transferGroup.putIfAbsent("02006",seoul6TF);
		Map<String,Set<String>> arriveGroup = new ConcurrentHashMap<>();
		arriveGroup.putIfAbsent("03144",wonhyoAR);
		arriveGroup.putIfAbsent("03004",shinyongsanAR);
		arriveGroup.putIfAbsent("03132",yongsan17AR);

		Map<Integer,Set<String>> depths = new ConcurrentHashMap<>();
		depths.putIfAbsent(1,departureGroup.keySet());
		depths.putIfAbsent(2,new HashSet<>());
		depths.putIfAbsent(3,new HashSet<>());
		depths.putIfAbsent(4,arriveGroup.keySet());

		// 프리패스를 먼저 확보한다. 이 과정에서 d1-d4 간선을 추가할 수 있다.
		List<List<String>> freepass = new ArrayList<>();
		departureGroup.forEach((dk,dv)->{
			arriveGroup.forEach((ak,av)->{
				List<List<String>> f = freepathSearch(dv,av,dk,ak);
				if(f.size()>0){
					freepass.addAll(f);
				}
			});
		});
//		List<String> fp = freepathSearch(pobangDP,wonhyoAR,"pobang","wonhyo");
//		List<String> fp2 =freepathSearch(pobangDP,shinyongsanAR,"pobang","shinyongsan");
//		List<String> fp3 =freepathSearch(pobangDP,yongsan17AR,"pobang","yongsan17");
//		List<String> fp4 = freepathSearch(ganhoDP,wonhyoAR,"ganho","wonhyo");
//		List<String> fp5 = freepathSearch(ganhoDP,shinyongsanAR,"ganho","shinyongsan");
//		List<String> fp6 = freepathSearch(ganhoDP,yongsan17AR,"ganho","yongsan17");
//		if(fp.size()>0){
//			freepass.add(fp);
//		} else if (fp2.size()>0){
//			freepass.add(fp2);
//		} else if (fp3.size()>0){
//			freepass.add(fp3);
//		} else if (fp4.size()>0){
//			freepass.add(fp4);
//		} else if (fp5.size()>0){
//			freepass.add(fp5);
//		} else if (fp6.size()>0){
//			freepass.add(fp6);
//		}
		freepass.forEach(l->l.forEach(s-> System.out.printf(" %s ",s)));
		System.out.println();

		// TF를 탐색하면서 뎁스를 확보한다. 이 과정에서 d1-d2 간선을 추가할 수 있다.
		Set<String> dps = new HashSet<>();
		Iterator<Set<String>> iterator = departureGroup.values().iterator();
		while(iterator.hasNext()){
			dps.addAll(iterator.next());
		}
//		dps.addAll(pobangDP);
//		dps.addAll(ganhoDP);
		System.out.println(dps);
		transferGroup.forEach((k,v)->{
			int depth = depthAllocation(dps,v);
			depths.compute(depth,(dk,e)->{
				e.add(k);
				return e;
			});
		});
//		hongjeDepth = depthAllocation(dps,hongjeTF);
//		harimgakDepth = depthAllocation(dps,harimgakTF);
//		seoulDepth = depthAllocation(dps, seoulTF);
//		seoul6Depth = depthAllocation(dps,seoul6TF);
//		int[] depths = new int[]{hongjeDepth,harimgakDepth,seoulDepth,seoul6Depth};
		System.out.println("=== depth ===");
		depths.forEach((k,v)-> System.out.println(k+":"+v));
//		Set<String>[] tfs = new Set[]{hongjeTF,harimgakTF,seoulTF,seoul6TF};
//		List<Integer> d2 = new ArrayList<>();
//		List<Integer> d3 = new ArrayList<>();
//		for(int i=0; i<depths.length; i++){
//			if(depths[i]==2){
//				d2.add(i);
//			} else {
//				d3.add(i);
//			}
//		}
		// 뎁스별 통합 셋 생성
		Set<String> depth2TF = new HashSet<>();
		Set<String> depth3TF = new HashSet<>();
		depths.get(2).forEach(k->depth2TF.addAll(transferGroup.get(k)));
		depths.get(3).forEach(k->depth3TF.addAll(transferGroup.get(k)));
//		for(int d : d2){
//			depth2TF.addAll(tfs[d]);
//		}
//		for(int d : d3){
//			depth3TF.addAll(tfs[d]);
//		}
		System.out.println("\n=== d2, d3 ===");
		System.out.println(depth2TF);
		System.out.println(depth3TF);

		// depth2 ~ arrive 경로 뽑기 - 이 과정에서 d2-d4 간선을 추가할 수 있다.
		List<List<String>> d2arpass = new ArrayList<>();
		depths.get(2).forEach(k->{
			arriveGroup.forEach((ak,av)->{
				List<List<String>> pass = freepathSearch(transferGroup.get(k),av,k,ak);
				if(pass.size()>0)
					d2arpass.addAll(pass);
			});
		});
//		for(int d: d2){
//			List<String> shin = freepathSearch(tfs[d],shinyongsanAR,"d2","shinyongsan");
//			List<String> won = freepathSearch(tfs[d],wonhyoAR,"d2","wonhyo");
//			List<String> y17 = freepathSearch(tfs[d],yongsan17AR,"d2","yongsan17");
//			if(shin.size()>0){
//				d2arpass.add(shin);
//			} else if (won.size()>0){
//				d2arpass.add(won);
//			} else if (y17.size()>0){
//				d2arpass.add(y17);
//			}
//		}

		// 출발지 필터링
		departureGroup.forEach((k,v)->v.retainAll(depth2TF));
//		pobangDP.retainAll(depth2TF);
//		ganhoDP.retainAll(depth2TF);

		// 도착지 필터링
		arriveGroup.forEach((k,v)->v.retainAll(depth3TF));
//		wonhyoAR.retainAll(depth3TF);
//		shinyongsanAR.retainAll(depth3TF);
//		yongsan17AR.retainAll(depth3TF);

		// 환승지 필터링
		dps.clear();
		departureGroup.forEach((k,v)->dps.addAll(v));
//		dps.addAll(pobangDP);
//		dps.addAll(ganhoDP);
		Set<String> ars = new HashSet<>();
		arriveGroup.forEach((k,v)->ars.addAll(v));
//		ars.addAll(wonhyoAR);
//		ars.addAll(shinyongsanAR);
//		ars.addAll(yongsan17AR);
		depth2TF.retainAll(depth3TF);
		depth3TF.retainAll(ars);
//		System.out.println(depth2TF);
//		System.out.println(depth3TF);
		dps.addAll(depth2TF);
		dps.addAll(depth3TF);
		transferGroup.forEach((k,v)->v.retainAll(dps));
//		for(Set tf : tfs){
//			tf.retainAll(dps);
//		}

		// 노선 방향에 따른 정렬 (환승역의 도착방향만 정렬. 출발방향도 함께 정렬하기 위해 그래프 도입결정)
		transferGroup.forEach((tk,tv)->{
			Set<String> sortedSet = new LinkedHashSet<>();
			arriveGroup.forEach((ak,av)->{
				Set<String> filtered = filteredAr(tv,av);
				sortedSet.addAll(filtered);
			});
			System.out.println(tk+" : "+sortedSet);
		});

		System.out.println("=== 최종 ===");
		departureGroup.forEach((k,v)-> System.out.println(v));
		transferGroup.forEach((k,v)-> System.out.println(v));
		arriveGroup.forEach((k,v)-> System.out.println(v));
		freepass.forEach(l->l.forEach(s-> System.out.printf(" %s ",s)));
		System.out.println();
		d2arpass.forEach(l->l.forEach(s-> System.out.printf(" %s ",s)));
//		System.out.println(pobangDP);
//		System.out.println(ganhoDP);
//		System.out.println(hongjeTF);
//		System.out.println(harimgakTF);
//		System.out.println(seoulTF);
//		System.out.println(seoul6TF);
//		System.out.println(wonhyoAR);
//		System.out.println(shinyongsanAR);
//		System.out.println(yongsan17AR);
//		for(List<String> p : freepass){
//			System.out.printf("%s, %s, %s",p.get(0),p.get(1),p.get(2));
//		}
//		System.out.println();
//		for(List<String> p : d2arpass){
//			for(String s : p){
//				System.out.printf(s);
//			}
//		}


	}

	@Test
	public void way4withGraph() {
		routes.putIfAbsent("13550",pobangDP);
		routes.putIfAbsent("13168",ganhoDP);
		routes.putIfAbsent("13028",hongjeTF);
		routes.putIfAbsent("01136",harimgakTF);
		routes.putIfAbsent("02004",seoulTF);
		routes.putIfAbsent("02006",seoul6TF);
		routes.putIfAbsent("03144",wonhyoAR);
		routes.putIfAbsent("03004",shinyongsanAR);
		routes.putIfAbsent("03132",yongsan17AR);

		Map<String, Set<String>> groups = new ConcurrentHashMap<>();
		groups.putIfAbsent("departure",new HashSet<>(Arrays.asList("13550","13168")));
		groups.putIfAbsent("transfer",new HashSet<>(Arrays.asList("13028","01136","02004","02006")));
		groups.putIfAbsent("arrive",new HashSet<>(Arrays.asList("03144","03004","03132")));

		Map<Integer,Set<String>> depths = new ConcurrentHashMap<>();
		depths.putIfAbsent(2,new HashSet<>());
		depths.putIfAbsent(3,new HashSet<>());

		RouteGraph graph = new RouteGraph();

		// 프리패스를 먼저 확보한다. 이 과정에서 d1-d4 간선을 추가할 수 있다.
		List<List<String>> freepass = createPassList(groups.get("departure"),groups.get("arrive"),graph);

		// TF를 탐색하면서 뎁스를 확보한다. 이 과정에서 d1-d2, d3-d4 간선 확보
		Set<String> depth2 = new HashSet<>();
		Set<String> depth3 = new HashSet<>();
		for(String tk : groups.get("transfer")){
			boolean isD2 = false;
			for(String dk : groups.get("departure")){
				List<List<String>> depth2Filter = edgeSearch(routes.get(dk), routes.get(tk), dk, tk, graph);
				if(depth2Filter.size()>0){
					depth2.add(tk);
					isD2 = true;
				}
			}
			if(!isD2) {
				for (String ak : groups.get("arrive")) {
					List<List<String>> depth3Filter = edgeSearch(routes.get(tk), routes.get(ak), tk, ak, graph);
					if (depth3Filter.size()>0)
						depth3.add(tk);
				}
			}
		}

		System.out.println("=== depth ===");
		System.out.println("d2: "+depth2);
		System.out.println("d3: "+depth3);

		// 뎁스별 통합 셋 생성
		Set<String> depth2TF = new HashSet<>();
		Set<String> depth3TF = new HashSet<>();
		depth2.forEach(k->depth2TF.addAll(routes.get(k)));
		depth3.forEach(k->depth3TF.addAll(routes.get(k)));

		System.out.println("=== d2, d3 ===");
		System.out.println(depth2TF);
		System.out.println(depth3TF);

		// depth2 ~ arrive 경로 뽑기 - 이 과정에서 d2-d4 간선을 추가할 수 있다.
		List<List<String>> d2arpass = createPassList(depth2,groups.get("arrive"),graph);

		// 출발지 필터링
		groups.get("departure").forEach(k->routes.get(k).retainAll(depth2TF));

		// 도착지 필터링
		groups.get("arrive").forEach(k->routes.get(k).retainAll(depth3TF));

		// 환승지 필터링
		Set<String> dps = new HashSet<>();
		groups.get("departure").forEach(k->dps.addAll(routes.get(k)));
		Set<String> ars = new HashSet<>();
		groups.get("arrive").forEach(k->ars.addAll(routes.get(k)));
		depth2TF.retainAll(depth3TF);
		depth3TF.retainAll(ars);
		dps.addAll(depth2TF);
		dps.addAll(depth3TF);
		groups.get("transfer").forEach(k->routes.get(k).retainAll(dps));

		// d2-d3 방면 탐색 후 그래프 완성
		createPassList(depth2,depth3,graph);

		// 프리패스 및 d2패스 추가
		addPass(freepass,routes);
		addPass(d2arpass,routes);

		// 노선 방면에 따른 정렬 (정렬이 필요한 경우 탐색 및 정렬)
		graph.getOutEdges().forEach((k,v)-> System.out.println("out_"+k+":"+v));
		for(String tk : groups.get("transfer")){
			int fanOut = graph.countFanOut(tk);
			if(fanOut>1){
				Set<String> sortedSet = sortRoutes(routes.get(tk),graph.findFanOutAdjNodes(tk),"out");
				System.out.println("outSort_"+tk+" : "+sortedSet);
				routes.put(tk,sortedSet);
			} else {
				int fanIn = graph.countFanIn(tk);
				if(fanIn>1){
					Set<String> sortedSet = sortRoutes(routes.get(tk),graph.findFanInAdjNodes(tk),"in");
					System.out.println("inSort_"+tk+" : "+sortedSet);
					routes.put(tk,sortedSet);
				}
			}
		}

		System.out.println("=== 최종 ===");
		List<FilteredBusDTO> lists = new ArrayList<>();
		groups.get("departure").forEach(k->{
			lists.add(createFilteredBus(k));
			System.out.println(routes.get(k));
		});
		groups.get("transfer").forEach(k-> {
			lists.add(createFilteredBus(k));
			System.out.println(routes.get(k));
		});
		groups.get("arrive").forEach(k->{
			lists.add(createFilteredBus(k));
			System.out.println(routes.get(k));
		});
		freepass.forEach(k-> System.out.println("freepass: "+k));
		d2arpass.forEach(k->System.out.println("d2pass: "+k));
		//paramsService.createFilteredBus(lists);

	}

	public FilteredBusDTO createFilteredBus(String k){
		System.out.println("in func: "+k);
		FilteredBusDTO dto = new FilteredBusDTO();
		String clusterName = paramsService.findByArsId(1L,k);
		dto.setJourneyNo(1L);
		dto.setClusterName(clusterName);
		dto.setArsId(k);
		dto.setRoutes(routes.get(k).toArray(new String[0]));
		return dto;
	}

	public List<List<String>> freepathSearch(Set<String> dp, Set<String> ar, String dk, String ak){
		List<List<String>> freepath = new ArrayList<>();
		for(String route : dp){
			List<String> pass = new ArrayList<>();
			if(ar.contains(route)){
				pass.add(route);
				pass.add(dk);
				pass.add(ak);
			}
			if(pass.size()>0){
				freepath.add(pass);
			}
		}
		return freepath;
	}

	public int depthAllocation(Set<String> dps, Set<String> tf){
		int depth = 0;
		boolean is2 = false;
		for(String dp : dps){
			if(tf.contains(dp)){   // 이 과정에서 출발지-해당d2 의 엣지를 추가할 수 있다.
				depth = 2;
				is2 = true;
				break;
			}
		}
		if(!is2){
			depth = 3;
		}
		return depth;
	}

	public Set<String> filteredAr(Set<String> tv, Set<String> av){
		Set<String> result = new LinkedHashSet<>();
		for(String route : tv){
			if(av.contains(route))
				result.add(route);
		}
		return result;
	}

	public List<List<String>> edgeSearch(Set<String> dp, Set<String> ar, String dk, String ak, RouteGraph graph){
		List<List<String>> freepath = new ArrayList<>();
		for(String route : dp){
			List<String> pass = new ArrayList<>();
			if(ar.contains(route)){
				pass.add(route);
				pass.add(dk);
				pass.add(ak);
				graph.addEdge(dk,ak);
			}
			if(pass.size()>0){
				freepath.add(pass);
			}
		}
		return freepath;
	}

	public List<List<String>> createPassList(Set<String> froms, Set<String> tos, RouteGraph graph) {
		List<List<String>> passList = new ArrayList<>();
		for(String fromK : froms){
			for(String toK : tos){
				List<List<String>> pass = edgeSearch(routes.get(fromK),routes.get(toK),fromK,toK,graph);
				if(pass.size()>0) {
					passList.addAll(pass);
				}
			}
		}
		return passList;
	}

	public List<Set<String>> addPass(List<List<String>> passes, Map<String,Set<String>> routes){
		List<Set<String>> sortedList = new ArrayList<>();
		passes.forEach(pass->{
			for(int i=1; i<3; i++){
				routes.computeIfPresent(pass.get(i),(key,set)-> {
					set.add(pass.get(0));
					return set;
				});
				sortedList.add(routes.get(i));
			}
		});
		return sortedList;
	}

	public Set<String> sortRoutes(Set<String> fromRoutes, Set<String> adjToNodes,String direction){
		Set<String> sortedRoutes = new LinkedHashSet<>();
		for(String adjK : adjToNodes){
			Set<String> toRoutes = new LinkedHashSet<>();
			Set<String> adjToRoutes = routes.get(adjK);
			for(String route : fromRoutes){
				if(adjToRoutes.contains(route))
					toRoutes.add(route);
			}
			if(toRoutes.size()>0) {
				sortedRoutes.addAll(toRoutes);
				fromRoutes.removeAll(toRoutes);
			}
		}
		if(fromRoutes.size()>0){
			sortedRoutes.addAll(fromRoutes);
			if("in".equals(direction)){
				List<String> list = new ArrayList<>(sortedRoutes);
				Collections.reverse(list);
				sortedRoutes = new LinkedHashSet<>(list);
			}
		}
		return sortedRoutes;
	}



}
