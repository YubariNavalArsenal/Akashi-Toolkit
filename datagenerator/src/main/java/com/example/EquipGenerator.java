package com.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.model.EquipImprovement;
import com.example.model.MultiLanguageEntry;
import com.example.model.NewEquip;
import com.example.network.RetrofitAPI;
import com.example.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.spreada.utils.chinese.ZHConverter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Retrofit;

/**
 * Created by Rikka on 2016/7/4.
 */
public class EquipGenerator {
    public static void main(String[] args) throws IOException {
        
        //System.setProperty("http.proxyHost", "127.0.0.1");
        //System.setProperty("http.proxyPort", "8888");
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://zh.kcwiki.org/")
                .build();
        
        RetrofitAPI.KcwikiService service = retrofit.create(RetrofitAPI.KcwikiService.class);
        //ResponseBody body = service.getPage("模块:舰娘装备数据改", "raw").execute().body();
        //Reader reader = body.charStream();
        //System.out.println(body.string());
        //https://zh.kcwiki.org/index.php?title=%E6%A8%A1%E5%9D%97:%E8%88%B0%E5%A8%98%E8%A3%85%E5%A4%87%E6%95%B0%E6%8D%AE%E6%94%B9&action=raw
        Reader reader = new FileReader(new File("L:\\NetBeans\\NetBeansProjects\\Akashi-Toolkit\\src\\装备.txt"));
        
        /*Document doc = Jsoup.connect("https://zh.kcwiki.org/index.php?title=%E6%A8%A1%E5%9D%97:%E8%88%B0%E5%A8%98%E8%A3%85%E5%A4%87%E6%95%B0%E6%8D%AE%E6%94%B9&action=raw")
                    .timeout(3000)
                    .get();
        Reader reader = new StringReader(doc.html());*/
        
        int count = 0;
        boolean comment = false;
        StringBuilder sb = new StringBuilder();
        for (int c = reader.read(); c != -1; c = reader.read()) {
            if ((char) c == '{') {
                count++;
            }

            if (count > 0 && !comment) {
                sb.append((char) c);
            }

            if ((char) c == '}') {
                count--;
            }
        }

        String str = sb.toString()
                .substring(2);
        
        com.example.utils.RWFile.writeLog(JSON.toJSONString(str),null);
        reader = new StringReader(str);
        sb = new StringBuilder();
        boolean skipSpace = true;
        for (int c = reader.read(); c != -1; c = reader.read()) {
            if (c == '"') {
                skipSpace = !skipSpace;
            }

            if (c != ' ' || !skipSpace) {
                sb.append((char) c);
            }
        }

        str = sb.toString()
                .replaceAll("--(.+)--", "")

                .replace("\r\n", "\n")
                .replace("\r", "")

                .replaceAll("\\[\"ネ\\(Ne\\)式引擎\"][^.]+\\}\\n", "")

                .replace("\t", "")
                .replace("\"?\"", "\"0\"")
                .replace("?", "")
                .replace("{}", "null")
                .replace("},\n}", "}\n}")

                .replaceAll("\\{\\[\"燃料\"]=(\\d+),\\[\"弹药\"]=(\\d+),\\[\"钢材\"]=(\\d+),\\[\"铝\"]=(\\d+)}", "[$1,$2,$3,$4]")
                .replaceAll("\\{\\[\"废弃燃料\"]=(\\d+),\\[\"废弃弹药\"]=(\\d+),\\[\"废弃钢材\"]=(\\d+),\\[\"废弃铝\"]=(\\d+)}", "[$1,$2,$3,$4]")

                .replaceAll("\\[\"([\\d\\]]+)\"\\]=\\{", "{\n[\"id\"]=\"$1\",")
                .replaceAll("\\[\"([^]]+)\"\\]=", "\"$1\":")
                .replaceAll("\"([^\"]+)\":\\{([^:=\\}]+)\\}", "\"$1\":[$2]")

                .replaceAll("\\{\"开发\":\\[(.+),(.+)],\"改修\":\\[(.+),(.+)],\"装备数\":(.+),\"装备\":\"(.+)\"}", "[$1,$2,$3,$4,$5,\"$6\"]")
                .replaceAll("\\{\"开发\":\\[(.+),(.+)],\"改修\":\\[(.+),(.+)],\"装备数\":(.+)}", "[$1,$2,$3,$4,$5]")
                .replaceAll("\\{\"装备\":\"(\\d+)\",\"等级\":(\\d+)}", "[\"$1\",$2]")

                .replace("\"日期\":{", "\"日期\":[")
                .replace("\"日\":", "")
                .replace("\"一\":", "")
                .replace("\"二\":", "")
                .replace("\"三\":", "")
                .replace("\"四\":", "")
                .replace("\"五\":", "")
                .replaceAll("\"六\":(\\[.+])\n},", "$1\n],")
                .replaceAll("\"六\":(\\[.+]),\n},", "$1\n],")

                .replace("{\n{\n", "[\n{\n")
                .replace("}\n}\n}", "}\n}\n]")

                .replace("\"无\"", "0")
                .replace("\"?\"", "0")
                .replace("\"短\"", "1")
                .replace("\"中\"", "2")
                .replace("\"长\"", "3")
                .replace("\"超长\"", "4")

                .replaceAll("长/(\\d)", "$1")

                .replace(",1,\"ネ(Ne)式引擎\"", "");

        Gson gson = new GsonBuilder()
                .create();
        
        str = "[" + str.substring(1, str.length() - 1) + "]";
        /*
        JSONArray jarr = JSON.parseArray(str);
        List<NewEquip> list = new ArrayList<>();
        for(Object obj:jarr) {
            JSONObject jobj = (JSONObject) obj;
            NewEquip entity= JSONObject.toJavaObject(jobj,NewEquip.class);
            list.add(entity);
        }
        */
        //List<NewEquip> list = JSON.parseObject(str, new TypeReference<List<NewEquip>>(){}.getType());
        JSONArray jarr = JSON.parseArray(str);
        com.example.utils.RWFile.writeLog(JSON.toJSONString(jarr),null);
        
        List<NewEquip> list = gson.fromJson(new StringReader(str), new TypeToken<List<NewEquip>>() {
        }.getType());
        
        list.removeAll(Collections.singleton(null));
        com.example.utils.RWFile.writeLog(JSON.toJSONString(list),null);
        
        for (NewEquip item : list) {
            item.setRarity(item.get稀有度().length());
            item.setName(new MultiLanguageEntry());
            item.getName().setZh_cn(item.getNameCN().trim());
            item.getName().setJa(item.getNameJP().trim());

            if (item.get装备改修() != null && item.get装备改修2() != null) {
                item.get装备改修().setShips();
                item.get装备改修2().setShips();
                item.setImprovements(new NewEquip.ImprovementEntity[]{item.get装备改修(), item.get装备改修2()});
            } else if (item.get装备改修() != null) {
                item.get装备改修().setShips();
                item.setImprovements(new NewEquip.ImprovementEntity[]{item.get装备改修()});
            }

            // 增加一个生成装备改修json的东西
            addEquipImprovement(item, item.getImprovements());
            new File("L:\\NetBeans\\NetBeansProjects\\Akashi-Toolkit\\datagenerator/data/equips/").mkdirs();
            File file = new File("datagenerator/data/equips/" + item.getNameCN().replace("/", "_") + ".txt");
            if (!file.exists()) {
                try {
                    //body = service.getPage(item.getNameCN(), "raw").execute().body();
                    String name = item.getNameCN();
                    if (name.contains("单装机枪")){}
                        //name = name.replace("单装机枪", "单装机铳");
                    String url = java.net.URLEncoder.encode(name,"utf-8");
                    Document doc = null;
                    try{
                        doc = Jsoup.connect("https://zh.kcwiki.org/index.php?title="+ url +"&action=raw")
                    .timeout(3000)
                    .get();
                    } catch (Exception ignored) {
                        continue;
                    }
                    if (doc == null)
                        continue;
                    if(doc.html().contains("重定向")){
                        int start = doc.html().indexOf("[[");
                        int end = doc.html().indexOf("]]");
                        name = doc.html().substring(start+2, end);
                        url = java.net.URLEncoder.encode(name,"utf-8");
                        try{
                            doc = Jsoup.connect("https://zh.kcwiki.org/index.php?title="+ url +"&action=raw")
                        .timeout(3000)
                        .get();
                        } catch (Exception ignored) {
                            continue;
                        }
                    }
                    
                    item.getName().setZh_cn(name);
                    sleep(500);
                    Utils.writeStreamToFile(new ByteArrayInputStream(doc.html().getBytes()), file.getPath());
                    //System.out.println();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    System.out.print(item.getNameCN());
                    System.out.println(" 炸裂了");
                    continue;
                }
            }

            String s = Utils.streamToString(new FileInputStream(file))
                    .replaceAll("\\|([^\\|]+) =", "\\|$1=");

            Pattern r;
            Matcher m;

            r = Pattern.compile("\\|图鉴说明原文=([^\\|}]+)");
            m = r.matcher(s);
            if (m.find()) {
                item.getIntroduction().setJa(a(m.group(1)));
            }

            r = Pattern.compile("\\|图鉴说明译文=([^\\|}]+)");
            m = r.matcher(s);
            if (m.find()) {
                item.getIntroduction().setZh_cn(a(m.group(1)));
            }
        }

        // 敌舰数据
        //reader = service.getPage("深海栖舰装备", "raw").execute().body().charStream();
        //https://zh.kcwiki.org/index.php?title=%E6%A8%A1%E5%9D%97:%E6%B7%B1%E6%B5%B7%E8%A3%85%E5%A4%87%E6%95%B0%E6%8D%AE&action=raw
        BufferedReader reader1 = new BufferedReader(new FileReader(new File("L:\\NetBeans\\NetBeansProjects\\Akashi-Toolkit\\src\\深海装备.txt")));
        String a;
        sb = new StringBuilder();
        while((a=reader1.readLine())!=null) {
             sb.append(a);
             sb.append("\n");
        }
        str = sb.toString()
                .replace("=短\"", "1")
                .replace("=中\"", "2")
                .replace("=长\"", "3")
                .replace("=超长\"", "4");

        Pattern r;
        Matcher m;
        r = Pattern.compile("\\{\\{深海装备列表\\|[^\\}]+}}");
        m = r.matcher(str);
        while (m.find()) {
            Matcher m2;

            NewEquip equip = new NewEquip();
            list.add(equip);

            str = m.group().replace("\n", "");

            m2 = Pattern.compile("编号=(\\d+)").matcher(str);
            if (m2.find())
                equip.setId(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("中文装备名字=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getName().setZh_cn(m2.group(1).trim());

            m2 = Pattern.compile("日文装备名字=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getName().setJa(m2.group(1).trim());

            m2 = Pattern.compile("图标=(\\d+)").matcher(str);
            if (m2.find()) {
                int type = Utils.stringToInt(m2.group(1));
                equip.setTypes(new int[]{type, type, type, type});
            }

            m2 = Pattern.compile("等级=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.setRarity(m2.group(1).length());

            m2 = Pattern.compile("射程=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setRange(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("火力=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setFirepower(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("装甲=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setArmor(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("雷装=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setTorpedo(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("回避=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setEvasion(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("对空=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setAA(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("对潜=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setASW(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("索敌=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setLOS(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("爆装=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setBombing(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("命中=([^\\|]+)").matcher(str);
            if (m2.find())
                equip.getAttr().setAccuracy(Utils.stringToInt(m2.group(1)));

            m2 = Pattern.compile("备注=([^\\|}]+)").matcher(str);
            if (m2.find())
                equip.setRemark(m2.group(1));
        }

        specialType(list);

        // 繁中
        
        for (NewEquip item : list) {
            item.getName().setZh_tw(ZHConverter.toTC(item.getName().getZh_cn()));
            item.getIntroduction().setZh_tw(ZHConverter.toTC(item.getIntroduction().getZh_cn()));
        }
        
        
        // 英语
        String page;
        Retrofit retrofit2 = new Retrofit.Builder()
                .baseUrl("http://en.kancollewiki.net/")
                .build();

        RetrofitAPI.KancollewikiService service2 = retrofit2.create(RetrofitAPI.KancollewikiService.class);
        page = service2.getPage("Equipment", "raw").execute().body().string();
        page += service2.getPage("List_of_equipment_used_by_the_enemy", "raw").execute().body().string();

        for (NewEquip item : list) {
            Pattern p = Pattern.compile(String.format("%d\\n.+\\n.+\\[\\[(.+)]]", item.getId()));
            Matcher m2 = p.matcher(page);
            if (m2.find()) {
                item.getName().setEn(m2.group(1).trim());
                System.out.println(item.getId() + " " + m2.group(1).trim());
            } else {
                p = Pattern.compile(String.format("%d\\n.+\\n.+\\n\\|(.+)", item.getId()));
                m2 = p.matcher(page);
                if (m2.find()) {
                    item.getName().setEn(m2.group(1).trim());
                    System.out.println(item.getId() + " " + m2.group(1).trim());
                } else {
                    System.out.println("!!!" + item.getName().getJa());
                }
            }

            if (item.getId() == 15) {
                item.getName().setEn("61cm Quad O² Torpedo Mount");
            } else if (item.getId() == 58) {
                item.getName().setEn("61cm Quint O² Torpedo Mount");
            } else if (item.getId() == 67) {
                item.getName().setEn("53cm Hull-mount O² Torpedoes");
            } else if (item.getId() == 125) {
                item.getName().setEn("61cm Triple O² Torpedo Mount");
            } else if (item.getId() == 108) {
                item.getName().setEn("Skilled Carrier-based Aircraft Maintenance Personnel");
            } else if (item.getId() == 83) {
                item.getName().setEn("Tenzan (931 Air Group)");
            } else if (item.getId() == 104) {
                item.getName().setEn("35.6cm Twin Gun Mount (Dazzle Camouflage)");
            } else if (item.getId() == 109) {
                item.getName().setEn("Zero Fighter Type 52 Type C (601 Air Group)");
            } else if (item.getId() == 116) {
                item.getName().setEn("Type 1 Armor-Piercing (AP) Shell");
            } else if (item.getId() == 122) {
                item.getName().setEn("10cm Twin High-angle Mount + Anti-Aircraft Fire Director");
            } else if (item.getId() == 127) {
                item.getName().setEn("Prototype FaT Type 95 O² Torpedo Kai");
            } else if (item.getId() == 130) {
                item.getName().setEn("12.7cm Twin High-angle Mount + Type 94 Anti-Aircraft Fire Director");
            } else if (item.getId() == 131) {
                item.getName().setEn("25mm Triple Autocannon Mount (Concentrated Deployment)");
            } else if (item.getId() == 179) {
                item.getName().setEn("Prototype 61cm Sextuple O² Torpedo Mount");
            } else if (item.getId() == 515) {
                item.getName().setEn("High-speed Abyssal Torpedo / 22inch Torpedo Mk.II");
            } else if (item.getId() == 536) {
                item.getName().setEn("Deteriorated AP Shell");
            }
        }

        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        str = gson.toJson(list)
                .replace("\"备注\"", "\"remark\"")
                .replace("\"属性\"", "\"attr\"")
                .replace("\"射程\"", "\"range\"")
                .replace("\"对空\"", "\"aa\"")
                .replace("\"装甲\"", "\"armor\"")
                .replace("\"对潜\"", "\"asw\"")
                .replace("\"回避\"", "\"evasion\"")
                .replace("\"火力\"", "\"fire\"")
                .replace("\"索敌\"", "\"los\"")
                .replace("\"雷装\"", "\"torpedo\"")
                .replace("\"爆装\"", "\"bomb\"")
                .replace("\"命中\"", "\"accuracy\"")
                .replace("\"废弃\"", "\"broken\"")
                .replace("\"改修备注\"", "\"remark\"")
                .replace("\"资源消费\"", "\"cost\"")
                .replace("\"初期消费\"", "\"item\"")
                .replace("\"中段消费\"", "\"item2\"")
                .replace("\"更新消费\"", "\"item3\"")
                .replace("\"更新装备\"", "\"upgrade\"")
                .replace("\"类别\"", "\"type\"")
                .replace("\"状态\"", "\"status\"")
                .replace("\"开发\"", "\"research\"")
                .replace("\"改修\"", "\"improvement\"")
                .replace("\"更新\"", "\"upgrade\"")
                .replace("\"熟练\"", "\"rank\"");


        Utils.objectToJsonFile(str, "L:/NetBeans/NetBeansProjects/Akashi-Toolkit/src/json/Equip.json");

        Utils.objectToJsonFile(equipImprovementList, "L:/NetBeans/NetBeansProjects/Akashi-Toolkit/src/json/EquipImprovement.json");
    }

    private static void specialType(List<NewEquip> list) {
        findByName("试制51cm连装炮", list).getTypes()[2] = 38; // 大口径主砲(II)
        findByName("试制景云（舰侦型）", list).getTypes()[2] = 94; // 艦上偵察機(II)
    }

    private static List<EquipImprovement> equipImprovementList = new ArrayList<>();

    private static void addEquipImprovement(NewEquip item, NewEquip.ImprovementEntity[] improvements) {
        if (improvements == null) {
            return;
        }

        for (NewEquip.ImprovementEntity improvement :
                improvements) {
            Map<Integer, List<Integer>> ships = new HashMap<>();
            Map<Integer, Integer> ids = new LinkedHashMap<>();

            for (List<Integer> entry : improvement.getShips()) {
                for (Integer id : entry) {
                    if (id >= 0) {
                        ids.put(id, 0);
                    }
                }
            }

            for (Map.Entry<Integer, Integer> entry : ids.entrySet()) {
                int id = entry.getKey();

                int data = 0;
                for (int i = 0; i < 7; i++) {
                    List<Integer> e = improvement.getShips().get(i);
                    for (Integer id2 : e) {
                        if (id2 == id) {
                            data |= (1 << i);
                        }
                    }
                }

                List<Integer> list = ships.get(data);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(id);
                ships.put(data, list);
            }

            EquipImprovement equipImprovement;
            if (improvement.getUpgrade() != null) {
                List<Integer> list = new ArrayList<>();
                list.add(improvement.getUpgrade()[0]);
                list.add(improvement.getUpgrade()[1]);
                equipImprovement = new EquipImprovement(item.getId(), list, ships);
            } else {
                equipImprovement = new EquipImprovement(item.getId(), ships);
            }

            if (item.getImprovements2() == null) {
                item.setImprovements2(new ArrayList<>());
            }
            item.getImprovements2().add(ships);
            equipImprovementList.add(equipImprovement);
        }
    }

    private static String a(String s) {
        if (s.contains("<poem>")) {
            return s.replace("<poem>", "")
                    .replace("</poem>", "")
                    .trim();
        } else {
            return s.replace("\n", "")
                    .replace("<br/>", "\n")
                    .replace("<br>", "\n")
                    .replace("<br />", "\n")
                    .trim();
        }
    }

    public static NewEquip findById(int id, List<NewEquip> ship) {
        for (NewEquip item : ship) {
            if (item.getId() == id) {
                return item;
            }
        }
        throw new IllegalArgumentException(id + " not found");
    }

    public static NewEquip findByName(String name, List<NewEquip> ship) {
        for (NewEquip item : ship) {
            if (name.equals(item.getNameCN()) || name.equals(item.getNameJP())) {
                return item;
            }
        }
        throw new IllegalArgumentException(name + " not found");
    }
}
