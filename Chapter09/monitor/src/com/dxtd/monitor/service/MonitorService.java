package com.dxtd.monitor.service;

import java.util.HashMap;
import java.util.Map;

import com.dxtd.monitor.util.RedisUtil;

import redis.clients.jedis.Jedis;

public class MonitorService {

	public String getInfo() {
		String infoContent = null;
		Jedis jedis = null;
		try {
			jedis = RedisUtil.getJedis();
			infoContent = jedis.info();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
		return infoContent;
	}

	public Double getIntValue(Map<String, String> infoMap, String key) {
		if (null == infoMap)
			return 0.0;

		Double value = Double.valueOf(infoMap.get(key));
		return value;
	}

	public String getStringValue(Map<String, String> infoMap, String key) {
		if (null == infoMap)
			return "";

		return infoMap.get(key).trim();
	}

	public Integer getKeys(String info, String key) {
		Integer keysNumValue = null;
		String[] strs = info.split("\n");
		if (strs != null && strs.length > 0) {
			for (int i = 0; i < strs.length; i++) {
				String s = strs[i].trim();

				if (s.indexOf(key) > -1) {
					String[] str = s.split(",");
					if (null != str) {
						String[] dbs = str[0].split(":");
						String[] dbKeys = dbs[1].split("=");
						String keyStr = dbKeys[0];
						keysNumValue = Integer.valueOf(dbKeys[1]);
						break;
					}
				}
			}
		}
		return keysNumValue;
	}

	public Map parseInfo(String content) {
		String[] lines = content.split("\n");
		Map infoMap = new HashMap();
		String part = null;
		for (String line : lines) {
			if (line.isEmpty()) {
				continue;
			}

			if (line.startsWith("#")) {
				part = line.replace("#", "").trim();
				continue;
			}

			int index = line.indexOf(':');
			if (index >= 0) {
				infoMap.put(part + "." + line.substring(0, index), line.substring(index + 1));
			}
		}

		return infoMap;
	}

	public void transData(String infoContent) {
		Map<String, String> infoMap = parseInfo(infoContent);
		// 内核空间占用CPU百分比
		String ucs = getStringValue(infoMap, "CPU.used_cpu_sys");
		// 用户空间占用CPU百分比
		String ucu = getStringValue(infoMap, "CPU.used_cpu_user");
		// 阻塞客户端数量
		String cbc = getStringValue(infoMap, "Clients.blocked_clients");
		// 连接客户端数量
		String ccc = getStringValue(infoMap, "Clients.connected_clients");
		// 使用总内存
		String mum = getStringValue(infoMap, "Memory.used_memory");
		// 使用物理内存
		String mur = getStringValue(infoMap, "Memory.used_memory_rss");
		// 运行以来执行过的命令的总数量
		String cmd = getStringValue(infoMap, "Stats.total_commands_processed");
		// 每秒过期key数量
		String exp = getStringValue(infoMap, "Stats.expired_keys");
		// 每秒淘汰key数量
		String evt = getStringValue(infoMap, "Stats.evicted_keys");
		// 每秒命中数量
		String hit = getStringValue(infoMap, "Stats.keyspace_hits");
		// 每秒丢失数量
		String mis = getStringValue(infoMap, "Stats.keyspace_misses");

		Integer db0keysNum = getKeys(infoContent, "db0:keys");

		long thisTs = System.currentTimeMillis();

		System.out.println("ucs=" + ucs + ",ucu=" + ucu + ",cbc=" + cbc);
		System.out.println("ccc=" + ccc + ",mum=" + mum + ",mur=" + mur);
		System.out.println("cmd=" + cmd + ",exp=" + exp);
		System.out.println("evt=" + evt + ",hit=" + hit + ",mis=" + mis);
		System.out.println("db0keysNum=" + db0keysNum);
	}

	public Map<String, Object> getRedisInfo(String infoContent) {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> infoMap = parseInfo(infoContent);
		// 使用总内存
		String mum = getStringValue(infoMap, "Memory.used_memory");
		// 连接客户端数量
		String ccc = getStringValue(infoMap, "Clients.connected_clients");
		// 使用物理内存
		String mur = getStringValue(infoMap, "Memory.used_memory_rss");
		// 运行以来执行过的命令的总数量
		String cmd = getStringValue(infoMap, "Stats.total_commands_processed");
		// 内核空间占用CPU百分比
		String ucs = getStringValue(infoMap, "CPU.used_cpu_sys");
		// 用户空间占用CPU百分比
		String ucu = getStringValue(infoMap, "CPU.used_cpu_user");
		Integer db0keysNum = getKeys(infoContent, "db0:keys");
		
		map.put("Memory.used_memory", mum);
		map.put("Clients.connected_clients", ccc);
		map.put("Memory.used_memory_rss", mur);
		map.put("Stats.total_commands_processed", cmd);
		map.put("CPU.used_cpu_sys", ucs);
		map.put("CPU.used_cpu_user", ucu);
		map.put("db0:keys", db0keysNum+"");
		
		return map;
	}

	public static void main(String[] args) {
		MonitorService monitor = new MonitorService();
		String info = monitor.getInfo();
		Map map = monitor.parseInfo(info);
		// System.out.println("map=" + map);
		//monitor.transData(info);
		Map redisInfo = monitor.getRedisInfo(info);
		System.out.println(redisInfo);
	}

}
