(function() {
  var expect;

  expect = chai.expect;

  describe("treetable()", function() {
    beforeEach(function() {
      this.subject = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='0' data-tt-branch='true'><td>N2</td></tr></table>");
    });

    it("maintains chainability", function() {
      expect(this.subject.treetable()).to.equal(this.subject);
    });

    it("adds treetable object to element", function() {
      expect(this.subject.data("treetable")).to.be.undefined;
      this.subject.treetable();
      expect(this.subject.data("treetable")).to.be.defined;
    });

    it("adds .treetable css class to element", function() {
      expect(this.subject.hasClass("treetable")).to.be.false;
      this.subject.treetable();
      expect(this.subject.hasClass("treetable")).to.be.true;
    });

    it("does not initialize twice", function() {
      var data;
      this.subject.treetable();
      data = this.subject.data("treetable");
      this.subject.treetable();
      expect(this.subject.data("treetable")).to.equal(data);
    });

    it("initializes twice when explicitly requested", function() {
      var newData, oldData;
      this.subject.treetable();
      oldData = this.subject.data("treetable");
      this.subject.treetable({}, true);
      newData = this.subject.data("treetable");
      expect(newData).not.to.equal(oldData);
      expect(this.subject.data("treetable")).to.equal(newData);
    });

    describe("destroy()", function() {
      it("removes treetable object from element", function() {
        this.subject.treetable();
        expect(this.subject.data("treetable")).to.be.defined;
        this.subject.treetable("destroy");
        expect(this.subject.data("treetable")).to.be.undefined;
      });

      it("removes .treetable css class from element", function() {
        this.subject.treetable();
        expect(this.subject.hasClass("treetable")).to.be.true;
        this.subject.treetable("destroy");
        expect(this.subject.hasClass("treetable")).to.be.false;
      });
    });

    describe("with expandable: false", function() {
      beforeEach(function() {
        this.subject.treetable({
          expandable: false
        }).appendTo("body");
      });

      afterEach(function() {
        this.subject.remove();
      });

      it("all nodes are visible", function() {
        var row, _i, _len, _ref, _results;
        _ref = this.subject[0].rows;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          row = _ref[_i];
          _results.push(expect($(row)).to.be.visible);
        }
        return _results;
      });
    });

    describe("with expandable: true and clickableNodeNames: false", function() {
      beforeEach(function() {
        this.subject.treetable({
          expandable: true,
          initialState: "expanded"
        }).appendTo("body");
      });

      afterEach(function() {
        this.subject.remove();
      });

      it("collapses branch when node toggler clicked", function() {
        expect(this.subject.treetable("node", 1).row).to.be.visible;
        this.subject.treetable("node", 0).row.find(".indenter a").click();
        expect(this.subject.treetable("node", 1).row).to.not.be.visible;
      });

      it("does not collapse branch when cell clicked", function() {
        expect(this.subject.treetable("node", 1).row).to.be.visible;
        this.subject.treetable("node", 0).row.find("td").first().click();
        expect(this.subject.treetable("node", 1).row).to.be.visible;
      });

      describe("for nodes with children", function() {
        it("renders a clickable node toggler", function() {
          expect(this.subject.treetable("node", 0).row).to.have("a");
        });
      });

      describe("for nodes without children", function() {
        it("does not render a clickable node toggler", function() {
          expect(this.subject.treetable("node", 1).row).to.not.have("a");
        });
      });

      describe("for nodes without children but with branch node data attribute", function() {
        it("renders a clickable node toggler", function() {
          expect(this.subject.treetable("node", 2).row).to.have("a");
        });
      });
    });

    describe("with expandable: true and clickableNodeNames: true", function() {
      beforeEach(function() {
        this.subject.treetable({
          expandable: true,
          clickableNodeNames: true
        }).appendTo("body");
      });

      afterEach(function() {
        this.subject.remove();
      });

      it("expands branch when node toggler clicked", function() {
        expect(this.subject.treetable("node", 1).row).to.not.be.visible;
        this.subject.treetable("node", 0).row.find(".indenter a").click();
        expect(this.subject.treetable("node", 1).row).to.be.visible;
      });

      it("expands branch when cell clicked", function() {
        expect(this.subject.treetable("node", 1).row).to.not.be.visible;
        this.subject.treetable("node", 0).row.find("td").first().click();
        expect(this.subject.treetable("node", 1).row).to.be.visible;
      });
    });

    describe("collapseAll()", function() {
      beforeEach(function() {
        this.subject.treetable({
          initialState: "expanded"
        });
      });

      it("collapses all nodes", function() {
        var row, _i, _len, _ref, _results;
        this.subject.treetable("collapseAll");
        _ref = this.subject[0].rows;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          row = _ref[_i];
          _results.push(expect($(row).hasClass("collapsed")).to.be.true);
        }
        return _results;
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("collapseAll")).to.equal(this.subject);
      });
    });

    describe("collapseNode()", function() {
      beforeEach(function() {
        this.subject.treetable({
          initialState: "expanded"
        });
      });

      it("collapses a root node", function() {
        var row = $(this.subject[0].rows[0]);
        this.subject.treetable("collapseNode", row.data("ttId"));
        expect(row.hasClass("collapsed")).to.be.true;
      });

      it("collapses a branch node", function() {
        var row = $(this.subject[0].rows[1]);
        this.subject.treetable("collapseNode", row.data("ttId"));
        expect(row.hasClass("collapsed")).to.be.true;
      });

      it("throws an error for unknown nodes", function() {
        var fn, subject;
        subject = this.subject;
        fn = function() {
          subject.treetable("collapseNode", "whatever");
        };
        expect(fn).to["throw"](Error, "Unknown node 'whatever'");
      });

      it("maintains chainability", function() {
        var row = $(this.subject[0].rows[0]);
        expect(this.subject.treetable("collapseNode", row.data("ttId"))).to.equal(this.subject);
      });
    });

    describe("expandAll()", function() {
      beforeEach(function() {
        this.subject.treetable({
          initialState: "collapsed"
        });
      });

      it("expands all nodes", function() {
        var row, _i, _len, _ref, _results;
        this.subject.treetable("expandAll");
        _ref = this.subject[0].rows;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
          row = _ref[_i];
          _results.push(expect($(row).hasClass("expanded")).to.be.true);
        }
        return _results;
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("expandAll")).to.equal(this.subject);
      });
    });

    describe("expandNode()", function() {
      beforeEach(function() {
        this.subject.treetable({
          initialState: "collapsed"
        });
      });

      it("expands a root node", function() {
        var row = $(this.subject[0].rows[0]);
        this.subject.treetable("expandNode", row.data("ttId"));
        expect(row.hasClass("expanded")).to.be.true;
      });

      it("expands a branch node", function() {
        var row = $(this.subject[0].rows[1]);
        this.subject.treetable("expandNode", row.data("ttId"));
        expect(row.hasClass("expanded")).to.be.true;
      });

      it("throws an error for unknown nodes", function() {
        var fn, subject;
        subject = this.subject;
        fn = function() {
          subject.treetable("expandNode", "whatever");
        };
        expect(fn).to["throw"](Error, "Unknown node 'whatever'");
      });

      it("maintains chainability", function() {
        var row = $(this.subject[0].rows[0]);
        expect(this.subject.treetable("expandNode", row.data("ttId"))).to.equal(this.subject);
      });
    });

    describe("loadBranch()", function() {
      beforeEach(function() {
        this.newRows = "<tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr><tr data-tt-id='4' data-tt-parent-id='2'><td>N4</td></tr>"
        this.moreRows = "<tr data-tt-id='5' data-tt-parent-id='2'><td>N5</td></tr>";

        this.subject.treetable();
        this.parentNode = this.subject.treetable("node", 2);
      });

      it("inserts rows into DOM, appending new rows to end of children", function() {
        expect(this.subject[0].rows.length).to.equal(3);
        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
        expect(this.subject[0].rows.length).to.equal(5);
        this.subject.treetable("loadBranch", this.parentNode, this.moreRows);
        expect(this.subject[0].rows.length).to.equal(6);

        // Verify order
        var order = _.map(this.subject[0].rows, function(row) { return $(row).data("ttId"); });
        expect(order).to.deep.equal([0,1,2,3,4,5]);
      });

      it("inserts rows after any descendants (#73)", function() {
        var childRows = "<tr data-tt-id='6' data-tt-parent-id='4'><td>N6</td></tr>";

        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
        this.subject.treetable("loadBranch", this.parentNode, childRows);
        this.subject.treetable("loadBranch", this.parentNode, this.moreRows);

        // Verify order
        var order = _.map(this.subject[0].rows, function(row) { return $(row).data("ttId"); });
        expect(order).to.deep.equal([0,1,2,3,4,6,5]);
      });

      it("does not choke when fed a collection object with rows instead of a string", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        this.subject.treetable("loadBranch", this.parentNode, $.parseHTML(this.newRows));
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
      });

      it("does not choke on leading whitespace", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        this.subject.treetable("loadBranch", this.parentNode, "   <tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr>");
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
      });

      it("does not choke on whitespace between rows", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        this.subject.treetable("loadBranch", this.parentNode, "<tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr>     <tr data-tt-id='4' data-tt-parent-id='2'><td>N4</td></tr>");
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
      });

      it("does not choke on non-row elements", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        this.subject.treetable("loadBranch", this.parentNode, "<b>Wish you were here</b><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr>");
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
      });

      it("inserts rows into tree", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        expect(this.subject.data("treetable").tree[4]).to.be.undefined;
        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
        expect(this.subject.data("treetable").tree[4]).to.be.defined;
      });

      it("registers nodes", function() {
        expect(this.subject.data("treetable").nodes.length).to.equal(3);
        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
        expect(this.subject.data("treetable").nodes.length).to.equal(5);
      });

      it("initializes nodes", function() {
        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
        expect(this.subject.data("treetable").tree[3].initialized).to.be.true;
        expect(this.subject.data("treetable").tree[4].initialized).to.be.true;
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("loadBranch", this.parentNode, this.newRows)).to.equal(this.subject);
      });

      describe("adding nodes at root level", function() {
        beforeEach(function() {
          this.rootRows = "<tr data-tt-id='6'><td>N6</td></tr>";
        });

        it("registers nodes as root nodes", function () {
          expect(this.subject.data("treetable").roots.length).to.equal(1);
          this.subject.treetable("loadBranch", null, this.rootRows);
          expect(this.subject.data("treetable").roots.length).to.equal(2);
        });

        it("inserts rows into DOM", function () {
          this.subject.treetable("loadBranch", null, this.rootRows);
          expect($(this.subject[0].rows[3]).data("ttId")).to.equal(6);
        });

        describe("when table uses a tbody element", function() {
          beforeEach(function() {
            this.subject = $("<table><tbody><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='0' data-tt-branch='true'><td>N2</td></tr></tbody></table>");
            this.subject.treetable();
          });

          it("appends nodes to tbody", function() {
            this.subject.treetable("loadBranch", null, this.rootRows);
            expect($(this.subject.find("tbody tr:last")).data("ttId")).to.equal(6);
          });
        });

        describe("when table uses tbody and tfoot elements", function() {
          beforeEach(function() {
            this.subject = $("<table><tbody><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='0' data-tt-branch='true'><td>N2</td></tr></tbody><tfoot><tr><td>Footer</td></tr></table>");
            this.subject.treetable();
          });

          it("still appends nodes to tbody", function() {
            this.subject.treetable("loadBranch", null, this.rootRows);
            expect($(this.subject.find("tbody tr:last")).data("ttId")).to.equal(6);
          });
        });
      });
    });

    describe("move()", function() {
      beforeEach(function() {
        this.subject.treetable();
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("move", 1, 2)).to.equal(this.subject);
      });
    });

    describe("node()", function() {
      beforeEach(function() {
        this.subject.treetable();
      });

      it("returns node by id", function() {
        expect(this.subject.treetable("node", "0")).to.equal(this.subject.data("treetable").tree[0]);
        expect(this.subject.treetable("node", 0)).to.equal(this.subject.data("treetable").tree[0]);
      });

      it("returns undefined for unknown node", function() {
        expect(this.subject.treetable("node", "unknown")).to.be.undefined;
      });
    });

    describe("reveal()", function() {
      beforeEach(function() {
        this.subject.treetable();
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("reveal", 2)).to.equal(this.subject);
      });
    });

    describe("unloadBranch()", function() {
      beforeEach(function() {
        this.newRows = "<tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr><tr data-tt-id='4' data-tt-parent-id='2'><td>N4</td></tr>"

        this.subject.treetable();
        this.parentNode = this.subject.treetable("node", 2);
        this.subject.treetable("loadBranch", this.parentNode, this.newRows);
      });

      it("removes rows from DOM", function() {
        expect(this.subject[0].rows.length).to.equal(5);
        this.subject.treetable("unloadBranch", this.parentNode);
        expect(this.subject[0].rows.length).to.equal(3);
      });

      it("removes rows from tree", function() {
        expect(this.subject.data("treetable").tree[3]).to.be.defined;
        expect(this.subject.data("treetable").tree[4]).to.be.defined;
        this.subject.treetable("unloadBranch", this.parentNode);
        expect(this.subject.data("treetable").tree[3]).to.be.undefined;
        expect(this.subject.data("treetable").tree[4]).to.be.undefined;
      });

      it("updates the branch and leaf classes", function() {
        this.subject.treetable("unloadBranch", this.subject.treetable("node", 0));
          expect($(this.subject[0].rows[0])).to.have.class('leaf');
      });

      it("updates the branch and leaf classes when has branchAttr", function() {
        this.subject.treetable("unloadBranch", this.parentNode);
          expect($(this.subject[0].rows[2])).to.have.class('branch');
      });

      it("removes nodes from node cache", function() {
        expect(this.subject.data("treetable").nodes.length).to.equal(5);
        this.subject.treetable("unloadBranch", this.parentNode);
        expect(this.subject.data("treetable").nodes.length).to.equal(3);
      });

      it("removes nodes from parent's list of children", function() {
        expect(this.parentNode.children.length).to.equal(2);
        this.subject.treetable("unloadBranch", this.parentNode);
        expect(this.parentNode.children.length).to.equal(0);
      });

      it("maintains chainability", function() {
        expect(this.subject.treetable("unloadBranch", this.parentNode)).to.equal(this.subject);
      });
    });

  });

  describe("TreeTable.Node", function() {
    describe("addChild()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='n0'><td>N0</td></tr><tr data-tt-id='n1'><td>N1</td></tr></table>");
        this.table.treetable();
        this.parent = this.table.data("treetable").tree["n0"];
        this.child = this.table.data("treetable").tree["n1"];
      });

      it("adds child to collection of children", function() {
        expect(this.parent.children).to.be.empty;
        this.parent.addChild(this.child);
        expect(this.parent.children).to.include(this.child);
      });
    });

    describe("ancestors()", function() {
      beforeEach(function() {
        this.subject = $("<table id='subject'><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr><tr data-tt-id='4' data-tt-parent-id='3'><td>N4</td></tr></table>").treetable().data("treetable").tree;
      });

      it("has correct size", function() {
        expect(_.size(this.subject[4].ancestors())).to.equal(3);
      });

      it("includes the parent node", function() {
        expect(this.subject[4].ancestors()).to.include(this.subject[4].parentNode());
      });

      it("includes the parent's parent node", function() {
        expect(this.subject[4].ancestors()).to.include(this.subject[3].parentNode());
      });

      it("includes the root node", function() {
        expect(this.subject[4].ancestors()).to.include(this.subject[1]);
      });

      it("does not include node itself", function() {
        expect(this.subject[4].ancestors()).to.not.include(this.subject[4]);
      });
    });

    describe("children", function() {
      beforeEach(function() {
        this.subject = $("<table id='subject'><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td><tr data-tt-id='5' data-tt-parent-id='2'><td>N5</td></tr></tr><tr data-tt-id='4' data-tt-parent-id='3'><td>N4</td></tr></table>").treetable().data("treetable").tree;
      });

      it("includes direct children", function() {
        expect(_.size(this.subject[2].children)).to.equal(2);
        expect(this.subject[2].children).to.include(this.subject[3]);
        expect(this.subject[2].children).to.include(this.subject[5]);
      });

      it("does not include grandchildren", function() {
        expect(this.subject[2].children).to.not.include(this.subject[4]);
      });

      it("does not include parent", function() {
        expect(this.subject[2].children).to.not.include(this.subject[2].parentNode());
      });

      it("does not include node itself", function() {
        expect(this.subject[2].children).to.not.include(this.subject[2]);
      });
    });

    describe("collapse()", function() {
      beforeEach(function() {
        this.table = $("<table id='subject'><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='0'><td>N2</td></tr><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr></table>").appendTo("body").treetable({
          initialState: "expanded"
        });
        this.subject = this.table.data("treetable").tree;
      });

      afterEach(function() {
        this.table.remove();
      });

      it("hides children", function() {
        expect(this.subject[1].row).to.be.visible;
        expect(this.subject[2].row).to.be.visible;
        this.subject[0].collapse();
        expect(this.subject[1].row).to.be.hidden;
        expect(this.subject[2].row).to.be.hidden;
      });

      it("recursively hides grandchildren", function() {
        expect(this.subject[3].row).to.be.visible;
        this.subject[0].collapse();
        expect(this.subject[3].row).to.be.hidden;
      });

      it("maintains chainability", function() {
        expect(this.subject[0].collapse()).to.equal(this.subject[0]);
      });
    });

    describe("expand()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='0'><td>N2</td></tr><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr></table>").appendTo("body").treetable({
          expandable: true
        });
        this.subject = this.table.data("treetable").tree;
      });

      afterEach(function() {
        this.table.remove();
      });

      it("shows children", function() {
        expect(this.subject[1].row).to.be.hidden;
        expect(this.subject[2].row).to.be.hidden;
        this.subject[0].expand();
        expect(this.subject[1].row).to.be.visible;
        expect(this.subject[2].row).to.be.visible;
      });

      it("does not recursively show collapsed grandchildren", function() {
        sinon.stub(this.subject[2], "expanded").returns(false);
        expect(this.subject[3].row).to.be.hidden;
        this.subject[0].expand();
        expect(this.subject[3].row).to.be.hidden;
      });

      it("recursively shows expanded grandchildren", function() {
        sinon.stub(this.subject[2], "expanded").returns(true);
        expect(this.subject[3].row).to.be.hidden;
        this.subject[0].expand();
        expect(this.subject[3].row).to.be.visible;
      });

      it("maintains chainability", function() {
        expect(this.subject[0].expand()).to.equal(this.subject[0]);
      });
    });

    describe("expanded()", function() {
      beforeEach(function() {
        this.subject = $("<table><tr data-tt-id='0'><td>Node</td></tr></table>").treetable().data("treetable").tree[0];
      });

      it("returns true when expanded", function() {
        this.subject.expand();
        expect(this.subject.expanded()).to.be["true"];
      });

      it("returns false when collapsed", function() {
        this.subject.collapse();
        expect(this.subject.expanded()).to.be["false"];
      });
    });

    describe("indenter", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>Root Node</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>Branch Node</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>Leaf Node</td></tr></table>").treetable({
          initialState: "expanded"
        }).data("treetable");
        this.rootNode = this.table.tree[0];
        this.branchNode = this.table.tree[1];
        this.leafNode = this.table.tree[2];
      });

      it("has the 'indenter' class", function() {
        expect(this.branchNode.indenter.hasClass("indenter")).to.be.true;
      });

      describe("when root node", function() {
        it("is not indented", function() {
          expect(this.rootNode.indenter.css("padding-left")).to.equal("0px");
        });
      });

      describe("when level 1 branch node", function() {
        it("is indented 19px", function() {
          expect(this.branchNode.indenter.css("padding-left")).to.equal("19px");
        });
      });

      describe("when level 2 leaf node", function() {
        it("is indented 38px", function() {
          expect(this.leafNode.indenter.css("padding-left")).to.equal("38px");
        });
      });
    });

    describe("initialized", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>Root Node</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>Leaf Node</td></tr></table>");
      });

      describe("when expandable is false", function() {
        beforeEach(function() {
          this.subject = this.table.treetable({
            expandable: false
          }).data("treetable").tree;
          this.rootNode = this.subject[0];
          this.leafNode = this.subject[1];
        });

        it("initializes root nodes immediately", function() {
          expect(this.rootNode.initialized).to.be["true"];
        });

        it("initializes non-root nodes immediately", function() {
          expect(this.leafNode.initialized).to.be["true"];
        });
      });

      describe("when expandable is true and initialState is 'collapsed'", function() {
        beforeEach(function() {
          this.subject = this.table.treetable({
            expandable: true,
            initialState: "collapsed"
          }).data("treetable").tree;
          this.rootNode = this.subject[0];
          this.leafNode = this.subject[1];
        });

        it("initializes root nodes immediately", function() {
          expect(this.rootNode.initialized).to.be["true"];
        });

        it("does not initialize non-root nodes immediately", function() {
          expect(this.leafNode.initialized).to.be["false"];
        });
      });

      describe("when expandable is true and initialState is 'expanded'", function() {
        beforeEach(function() {
          this.subject = this.table.treetable({
            expandable: true,
            initialState: "expanded"
          }).data("treetable").tree;
          this.rootNode = this.subject[0];
          this.leafNode = this.subject[1];
        });

        it("initializes root nodes immediately", function() {
          expect(this.rootNode.initialized).to.be["true"];
        });

        it("initializes non-root nodes immediately", function() {
          expect(this.leafNode.initialized).to.be["true"];
        });
      });
    });

    describe("hide()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr></table>").appendTo("body").treetable();
        this.subject = this.table.data("treetable").tree;
        this.subject[0].expand();
      });

      afterEach(function() {
        this.table.remove();
      });

      it("hides table row", function() {
        expect(this.subject[0].row).to.be.visible;
        this.subject[0].hide();
        expect(this.subject[0].row).to.be.hidden;
      });

      it("recursively hides children", function() {
        expect(this.subject[1].row).to.be.visible;
        this.subject[0].hide();
        expect(this.subject[1].row).to.be.hidden;
      });

      it("maintains chainability", function() {
        expect(this.subject[0].hide()).to.equal(this.subject[0]);
      });
    });

    describe("id", function() {
      it("is extracted from row attributes", function() {
        var subject;
        subject = $("<table><tr data-tt-id='42'><td>N42</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.id).to.equal(42);
      });
    });

    describe("isBranchNode()", function() {
      it("is true when node has children", function() {
        var subject = $("<table><tr data-tt-id='42'><td>N42</td></tr><tr data-tt-id='21' data-tt-parent-id='42'><td>N21</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.isBranchNode()).to.be.true;
      });

      it("is true when node has data attribute tt-branch with value 'true'", function() {
        var subject = $("<table><tr data-tt-id='42' data-tt-branch='true'><td>N42</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.isBranchNode()).to.be.true;
      });

      // This would be an error in the tree, but I consider having children
      // more important than the ttBranch attribute.
      it("is true when node has children but also a tt-branch attribute with value 'false'", function() {
        var subject = $("<table><tr data-tt-id='42' data-tt-branch='false'><td>N42</td></tr><tr data-tt-id='21' data-tt-parent-id='42'><td>N21</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.isBranchNode()).to.be.true;
      });

      it("is false when node has data attribute tt-branch with value 'false'", function() {
        var subject = $("<table><tr data-tt-id='42' data-tt-branch='false'><td>N42</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.isBranchNode()).to.be.false;
      });

      it("is false when node has no children and no tt-branch attribute", function() {
        var subject = $("<table><tr data-tt-id='42'><td>N42</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.isBranchNode()).to.be.false;
      });
    });

    describe("level()", function() {
      beforeEach(function() {
        this.subject = $("<table id='subject'><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr><tr data-tt-id='3' data-tt-parent-id='2'><td>N3</td></tr><tr data-tt-id='4' data-tt-parent-id='3'><td>N4</td></tr></table>").treetable().data("treetable").tree;
      });

      it("equals the number of ancestors", function() {
        expect(this.subject[1].level()).to.equal(0);
        expect(this.subject[2].level()).to.equal(1);
        expect(this.subject[3].level()).to.equal(2);
        expect(this.subject[4].level()).to.equal(3);
      });
    });

    describe("parentId", function() {
      it("is extracted from row attributes", function() {
        var subject;
        subject = $("<table><tr data-tt-id='12'><td>N12</td></tr><tr data-tt-id='42' data-tt-parent-id='12'><td>N42</td></tr></table>").treetable().data("treetable").tree[42];
        expect(subject.parentId).to.equal(12);
      });

      it("is undefined when not available", function() {
        var subject;
        subject = $("<table><tr data-tt-id='0'><td>N42</td></tr></table>").treetable().data("treetable").tree[0];
        expect(subject.parentId).to.be.undefined;
      });

      it("is undefined when empty", function() {
        var subject;
        subject = $("<table><tr data-tt-id='0' data-tt-parent-id=''><td>N42</td></tr></table>").treetable().data("treetable").tree[0];
        expect(subject.parentId).to.be.undefined;
      });
    });

    describe("parentNode()", function() {
      beforeEach(function() {
        this.subject = $("<table id='subject'><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr></table>").treetable().data("treetable").tree;
      });

      describe("when node has a parent", function() {
        beforeEach(function() {
          this.subject = this.subject[1];
        });

        it("is a node object", function() {
          // to.be.an.instanceof fails in IE9, is this a chai bug?
          expect(this.subject.parentNode()).that.is.an.instanceof(TreeTable.Node);
        });

        it("'s id equals this node's parentId", function() {
          expect(this.subject.parentNode().id).to.equal(this.subject.parentId);
        });
      });

      describe("when node has no parent", function() {
        beforeEach(function() {
          this.subject = this.subject[0];
        });

        it("is null", function() {
          expect(this.subject.parentNode()).to.be.null;
        });
      });
    });

    describe("removeChild()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='n0'><td>N0</td></tr><tr data-tt-id='n1' data-tt-parent-id='n0'><td>N1</td></tr></table>");
        this.table.treetable();
        this.parent = this.table.data("treetable").tree["n0"];
        this.child = this.table.data("treetable").tree["n1"];
      });

      it("removes child from collection of children", function() {
        expect(this.parent.children).to.include(this.child);
        this.parent.removeChild(this.child);
        expect(this.parent.children).to.be.empty;
      });
    });

    describe("render()", function() {
      it("maintains chainability", function() {
        var subject;
        subject = $("<table><tr data-tt-id='n0'><td>N0</td></tr><tr data-tt-id='n1' data-tt-parent-id='n0'><td>N1</td></tr></table>").treetable().data("treetable").tree["n0"];
        expect(subject.render()).to.equal(subject);
      });
    });

    describe("setParent()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='n0'><td>N0</td></tr><tr data-tt-id='n1' data-tt-parent-id='n0'><td>N1</td></tr><tr data-tt-id='n2'><td>N2</td></tr></table>");
        this.table.treetable();
        this.oldParent = this.table.data("treetable").tree["n0"];
        this.subject = this.table.data("treetable").tree["n1"];
        this.newParent = this.table.data("treetable").tree["n2"];
      });

      it("updates node's parent id", function() {
        expect(this.subject.parentId).to.equal("n0");
        this.subject.setParent(this.newParent);
        expect(this.subject.parentId).to.equal("n2");
      });

      it("updates node's parent id data attribute", function() {
        expect(this.subject.row.data("ttParentId")).to.equal("n0");
        this.subject.setParent(this.newParent);
        expect(this.subject.row.data("ttParentId")).to.equal("n2");
      });

      it("adds node to new parent's children", function() {
        this.subject.setParent(this.newParent);
        expect(this.newParent.children).to.include(this.subject);
      });

      it("removes node from old parent's children", function() {
        this.subject.setParent(this.newParent);
        expect(this.oldParent.children).to.not.include(this.subject);
      });

      it("does not try to remove children from parent when node is a root node", function() {
        var fn, newParent, subject;
        subject = this.subject;
        newParent = this.newParent;
        fn = function() {
          subject.setParent(newParent);
        };
        expect(fn).to.not["throw"](Error);
      });
    });

    describe("show()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr></table>").appendTo("body").treetable();
        this.subject = this.table.data("treetable").tree;
        this.subject[0].hide();
      });

      afterEach(function() {
        this.table.remove();
      });

      it("shows table row", function() {
        expect(this.subject[0].row).to.be.hidden;
        this.subject[0].show();
        expect(this.subject[0].row).to.be.visible;
      });

      it("maintains chainability", function() {
        expect(this.subject[0].show()).to.equal(this.subject[0]);
      });

      describe("when expanded", function() {
        beforeEach(function() {
          this.subject[0].expand().hide();
        });

        it("recursively shows children", function() {
          expect(this.subject[1].row).to.be.hidden;
          this.subject[0].show();
          expect(this.subject[1].row).to.be.visible;
        });
      });

      describe("when collapsed", function() {
        beforeEach(function() {
          this.subject[0].collapse().hide();
        });

        it("does not show children", function() {
          expect(this.subject[1].row).to.be.hidden;
          this.subject[0].show();
          expect(this.subject[1].row).to.be.hidden;
        });
      });
    });

    describe("toggle()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='42'><td>N42</td></tr><tr data-tt-id='24' data-tt-parent-id='42'><td>N24</td></tr></table>").appendTo("body").treetable({
          expandable: true
        });
        this.subject = this.table.data("treetable").tree;
      });

      afterEach(function() {
        this.table.remove();
      });

      it("toggles child rows", function() {
        expect(this.subject[24].row).to.be.hidden;
        this.subject[42].toggle();
        expect(this.subject[24].row).to.be.visible;
        this.subject[42].toggle();
        expect(this.subject[24].row).to.be.hidden;
      });

      it("maintains chainability", function() {
        expect(this.subject[42].toggle()).to.equal(this.subject[42]);
      });
    });

    describe("treeCell", function() {
      describe("with default column setting", function() {
        beforeEach(function() {
          this.subject = $("<table><tr data-tt-id='0'><th>Not part of tree</th><td>Column 1</td><td>Column 2</td></tr>").treetable().data("treetable").tree[0].treeCell;
        });

        it("is an object", function() {
          // to.be.an("object") fails in IE9, is this a chai bug?
          expect(this.subject).that.is.an("object");
        });

        it("maps to a td", function() {
          expect(this.subject).to.be("td");
        });

        it("maps to the first column by default", function() {
          expect(this.subject).to.contain("Column 1");
        });

        it("contains an indenter", function() {
          expect(this.subject).to.have("span.indenter");
        });
      });

      describe("with custom column setting", function() {
        beforeEach(function() {
          this.subject = $("<table><tr data-tt-id='0'><th>Not part of tree</th><td>Column 1</td><td>Column 2</td></tr></table>").treetable({
            column: 1
          }).data("treetable").tree[0].treeCell;
        });

        it("is configurable", function() {
          expect(this.subject).to.contain("Column 2");
        });
      });
    });
  });

  describe("TreeTable.Tree", function() {
    describe("loadRows()", function() {
      it("maintains chainability", function() {
        var subject = new TreeTable.Tree($("<table></table>"), {});
        expect(subject.loadRows()).to.equal(subject);
      });

      describe("a table without rows", function() {
        it("'s tree cache is empty", function() {
          var subject = new TreeTable.Tree($("<table></table>"), {}).loadRows().tree;
          expect(_.size(subject)).to.equal(0);
        });
      });

      describe("a table with tree rows", function() {
        beforeEach(function() {
            this.subject = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr></table>").treetable().data("treetable").tree;
        });
        it("caches all tree nodes", function() {
          expect(_.size(this.subject)).to.equal(2);
          expect(_.keys(this.subject)).to.include('0');
          expect(_.keys(this.subject)).to.include('1');
        });

        it("sets branch and leaf classes", function() {
            expect(this.subject[0].row).to.have.class('branch');
            expect(this.subject[1].row).to.have.class('leaf');
        });
      });

      describe("a table without tree rows", function() {
        it("results in an empty node cache", function() {
          var subject;
          subject = $("<table><tr></tr><tr></tr></table>").treetable().data("treetable").tree;
          expect(_.size(subject)).to.equal(0);
        });
      });

      describe("a table with both tree rows and non tree rows", function() {
        it("only caches tree nodes", function() {
          var subject;
          subject = $("<table><tr></tr><tr data-tt-id='21'><td>N21</td></tr></table>").treetable().data("treetable").tree;
          expect(_.size(subject)).to.equal(1);
          expect(_.keys(subject)).to.include('21');
        });
      });
    });

    describe("move()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='n0'><td>N0</td></tr><tr data-tt-id='n1' data-tt-parent-id='n0'  data-tt-branch='true'><td>N1</td></tr><tr data-tt-id='n2' data-tt-parent-id='n1'><td>N2</td></tr><tr data-tt-id='n3'><td>N3</td></tr><tr data-tt-id='n4' data-tt-parent-id='n3'><td>N4</td></tr><tr data-tt-id='n5' data-tt-parent-id='n3'><td>N5</td></tr></table>");
        this.table.treetable();
      });

      it("moves node to new destination", function() {
        var subject;
        subject = this.table.data("treetable").tree["n2"];
        expect(subject.parentId).to.equal("n1");
        this.table.treetable("move", "n2", "n3");
        expect(subject.parentId).to.equal("n3");
      });

      it("updates branch and leaf classes when node has siblings", function() {
        this.table.treetable("move", "n5", "n0");
        expect(this.table.data("treetable").tree["n0"].row).to.have.class('branch');
        expect(this.table.data("treetable").tree["n3"].row).to.have.class('branch');
        expect(this.table.data("treetable").tree["n5"].row).to.have.class('leaf');
      });

      it("updates branch and leaf classes when move when node has no siblings", function() {
        this.table.treetable("move", "n1", "n3");
        expect(this.table.data("treetable").tree["n0"].row).to.have.class('leaf');
        expect(this.table.data("treetable").tree["n1"].row).to.have.class('branch');
        expect(this.table.data("treetable").tree["n3"].row).to.have.class('branch');
      });

      it("updates branch and leaf classes when move when destination node has no siblings", function() {
        expect(this.table.data("treetable").tree["n5"].row).to.have.class('leaf');
        this.table.treetable("move", "n4", "n5");
        expect(this.table.data("treetable").tree["n3"].row).to.have.class('branch');
        expect(this.table.data("treetable").tree["n4"].row).to.have.class('leaf');
        expect(this.table.data("treetable").tree["n5"].row).to.have.class('branch');
      });

      it("updates branch and leaf classes when move form branchAttr", function() {
        this.table.treetable("move", "n2", "n0");
        expect(this.table.data("treetable").tree["n1"].row).to.have.class('branch');
        expect(this.table.data("treetable").tree["n2"].row).to.have.class('leaf');
      });

      it("cannot make node a descendant of itself", function() {
        var fn, table;
        table = this.table;
        fn = function() {
          table.treetable("move", "n1", "n2");
        };
        expect(fn).to.not.throw();
      });

      it("cannot make node a child of itself", function() {
        var fn, table;
        table = this.table;
        fn = function() {
          table.treetable("move", "n1", "n1");
        };
        expect(fn).to.not.throw();
      });

      it("does nothing when node is moved to current location", function() {
        // TODO How to test? Nothing is happening...
        this.table.treetable("move", "n1", "n0");
      });

      it("maintains chainability", function() {
        var destination, node, tree;
        tree = this.table.data("treetable");
        node = this.table.data("treetable").tree["n1"];
        destination = this.table.data("treetable").tree["n3"];
        expect(tree.move(node, destination)).to.equal(tree);
      });
    });

    describe("render()", function() {
      it("maintains chainability", function() {
        var subject;
        subject = new TreeTable.Tree($("<table></table>"), {});
        expect(subject.render()).to.equal(subject);
      });
    });

    describe("reveal()", function() {
      beforeEach(function() {
        this.table = $("<table><tr data-tt-id='0'><td>N0</td></tr><tr data-tt-id='1' data-tt-parent-id='0'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable({
          expandable: true
        }).appendTo("body");
        this.subject = this.table.data("treetable");
      });

      afterEach(function() {
        this.table.remove();
      });

      it("reveals a node", function() {
        expect(this.subject.tree[2].row).to.not.be.visible;
        this.table.treetable("reveal", 2);
        expect(this.subject.tree[2].row).to.be.visible;
      });

      it("expands the ancestors of the node", function() {
        expect(this.subject.tree[1].row).to.not.be.visible;
        this.table.treetable("reveal", 2);
        expect(this.subject.tree[1].row).to.be.visible;
      });

      it("throws an error for unknown nodes", function() {
        var fn, table;
        table = this.table;
        fn = function() {
          table.treetable("reveal", "whatever");
        };
        expect(fn).to["throw"](Error, "Unknown node 'whatever'");
      });
    });

    describe("roots", function() {
      describe("when no rows", function() {
        it("is empty", function() {
          var subject;
          subject = $("<table></table>").treetable().data("treetable");
          expect(_.size(subject.roots)).to.equal(0);
        });
      });

      describe("when single root node", function() {
        beforeEach(function() {
          this.subject = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable().data("treetable");
        });

        it("includes root node when only one root node exists", function() {
          var roots;
          roots = this.subject.roots;
          expect(_.size(roots)).to.equal(1);
          expect(roots).to.include(this.subject.tree[1]);
        });

        it("does not include non-root nodes", function() {
          expect(this.subject.roots).to.not.include(this.subject.tree[2]);
        });
      });

      describe("when multiple root nodes", function() {
        beforeEach(function() {
          this.subject = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr><tr data-tt-id='3'><td>N3</td></tr></table>").treetable().data("treetable");
        });

        it("includes all root nodes", function() {
          var roots;
          roots = this.subject.roots;
          expect(_.size(roots)).to.equal(2);
          expect(roots).to.include(this.subject.tree[1]);
          expect(roots).to.include(this.subject.tree[3]);
        });

        it("does not include non-root nodes", function() {
          expect(this.subject.roots).to.not.include(this.subject.tree[2]);
        });
      });
    });
  });

  describe("events", function() {
    describe("onInitialized", function() {
      describe("when no callback function given", function() {
        it("does not complain", function() {
          var table;
          table = $("<table><tr data-tt-id='1'><td>N1</td></tr></table>").treetable({
            onInitialized: null
          });
        });
      });

      describe("when callback function given", function() {
        it("is called when tree has been initialized", function() {
          var callback, table;

          callback = sinon.spy();
          table = $("<table><tr data-tt-id='1'><td>N1</td></tr></table>").treetable({
            onInitialized: callback
          });

          expect(callback.called).to.be.true;
        });
      });
    });

    describe("onNodeCollapse", function() {
      describe("when no callback function given", function() {
        it("does not complain", function() {
          var table;
          table = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable({
            initialState: "expanded",
            onNodeCollapse: null
          }).data("treetable");
          table.roots[0].collapse();
        });
      });

      describe("when callback function given", function() {
        beforeEach(function() {
          this.callback = sinon.spy();
          this.table = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable({
            initialState: "expanded",
            onNodeCollapse: this.callback
          }).data("treetable");
        });

        it("is called when node is being hidden", function() {
          this.table.roots[0].collapse();
          expect(this.callback.called).to.be["true"];
        });

        it("is not called when node is being shown", function() {
          this.table.roots[0].expand();
          expect(this.callback.called).to.be["false"];
        });

        it("is not called when node is not initialized yet", function() {
          this.table.roots[0].initialized = false;
          this.table.roots[0].collapse();
          expect(this.callback.called).to.be["false"];
        });
      });
    });

    describe("onNodeInitialized", function() {
      describe("when no callback function given", function() {
        it("does not complain", function() {
          var table;
          table = $("<table><tr data-tt-id='1'><td>N1</td></tr></table>").treetable({
            onNodeInitialized: null
          }).data("treetable");
          table.roots[0].initialized = false;
          table.roots[0].show();
        });
      });

      describe("when callback function given", function() {
        beforeEach(function() {
          this.callback = sinon.spy();
          this.table = $("<table><tr data-tt-id='1'><td>N1</td></tr></table>").treetable({
            onNodeInitialized: this.callback
          }).data("treetable");
        });

        it("is called when node is not initialized yet", function() {
          this.table.roots[0].initialized = false;
          this.table.roots[0].show();
          expect(this.callback.called).to.be.true;
        });

        it("is not called again when node is already initialized", function() {
          this.table.roots[0].show();
          // Node was initialized before, callback has already been called. I
          // check that the callback is not called more than once.
          expect(this.callback.calledOnce).to.be.true;
        });
      });
    });

    describe("onNodeExpand", function() {
      describe("when no callback given", function() {
        it("does not complain", function() {
          var table;
          table = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable({
            initialState: "expanded",
            onNodeExpand: null
          }).data("treetable");
          table.roots[0].expand();
        });
      });

      describe("when callback function given", function() {
        beforeEach(function() {
          this.callback = sinon.spy();
          this.table = $("<table><tr data-tt-id='1'><td>N1</td></tr><tr data-tt-id='2' data-tt-parent-id='1'><td>N2</td></tr></table>").treetable({
            initialState: "expanded",
            onNodeExpand: this.callback
          }).data("treetable");
        });

        it("is called when node is being shown", function() {
          this.table.roots[0].expand();
          expect(this.callback.called).to.be["true"];
        });

        it("is not called when node is being hidden", function() {
          this.table.roots[0].collapse();
          expect(this.callback.called).to.be["false"];
        });

        it("is not called when node is not initialized yet", function() {
          this.table.roots[0].initialized = false;
          this.table.roots[0].expand();
          expect(this.callback.called).to.be["false"];
        });
      });
    });
  });
}).call(this);
